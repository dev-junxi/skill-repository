#!/usr/bin/env bash
# skill-repository 的 CRUD 工具。给 agent 用，也可以人手跑。
#
#   skill-repo.sh list [分类]              列出 skill
#   skill-repo.sh search <关键词>          按名称/描述搜索
#   skill-repo.sh install <name> [目标目录] 安装到本地 agent
#   skill-repo.sh update <name>            更新已安装的
#   skill-repo.sh upload <本地目录> <分类>  上传（走分支 + PR）
#   skill-repo.sh remove <name>            从仓库删除（走分支 + PR）
#   skill-repo.sh uninstall <name>         只删本地，不动仓库
#
# 依赖：gh（已登录）、python3、git
set -euo pipefail

REPO="${SKILL_REPO:-dev-junxi/skill-repository}"
CACHE="${TMPDIR:-/tmp}/skill-repo-registry.json"

# 本地 skills 目录：按 agent 类型自动判断，也可用 SKILLS_DIR 覆盖
detect_skills_dir() {
  if [ -n "${SKILLS_DIR:-}" ]; then printf '%s' "$SKILLS_DIR"; return; fi
  for d in "$HOME/.claude/skills" "$HOME/.hermes/skills" "$HOME/.openclaw/skills"; do
    [ -d "$d" ] && { printf '%s' "$d"; return; }
  done
  printf '%s' "$HOME/.claude/skills"   # 都不存在时的默认
}
SKILLS_DIR="$(detect_skills_dir)"

need() { command -v "$1" >/dev/null || { echo "缺少依赖: $1" >&2; exit 2; }; }
need gh; need python3; need git

fetch_registry() {
  gh api "repos/$REPO/contents/registry.json" --jq '.content' 2>/dev/null \
    | base64 -d > "$CACHE" || { echo "拉取 registry 失败：确认 $REPO 存在且 gh 已登录" >&2; exit 2; }
}

# 按 name 查 skill，输出 "category/path"，找不到返回非 0
resolve() {
  python3 - "$CACHE" "$1" <<'PY'
import json, sys
reg, name = sys.argv[1], sys.argv[2]
r = json.load(open(reg))
hit = [s for s in r['skills'] if s['name'] == name]
if not hit:
    near = [s['name'] for s in r['skills'] if name.lower() in s['name'].lower()]
    print(f"未找到 skill '{name}'" + (f"；相近的：{', '.join(near[:5])}" if near else ""), file=sys.stderr)
    sys.exit(1)
print(hit[0]['path'])
PY
}

cmd_list() {
  fetch_registry
  python3 - "$CACHE" "${1:-}" <<'PY'
import json, sys
r = json.load(open(sys.argv[1]))
want = sys.argv[2] if len(sys.argv) > 2 else ""
if want and want not in r['categories']:
    sys.exit(f"未知分类 '{want}'；可选：{', '.join(sorted(r['categories']))}")
for cat, meta in r['categories'].items():
    if want and cat != want: continue
    items = [s for s in r['skills'] if s['category'] == cat]
    if not items and not want: continue
    print(f"\n{meta['emoji']} {cat} — {meta['label']} ({len(items)})")
    for s in items:
        print(f"   • {s['name']}\n     {s['description'][:100]}")
print(f"\n共 {r['total']} 个 skill")
PY
}

cmd_search() {
  [ -n "${1:-}" ] || { echo "用法: skill-repo.sh search <关键词>" >&2; exit 2; }
  fetch_registry
  python3 - "$CACHE" "$1" <<'PY'
import json, sys
r = json.load(open(sys.argv[1])); q = sys.argv[2].lower()
hits = [s for s in r['skills'] if q in s['name'].lower() or q in s['description'].lower()]
if not hits: sys.exit(f"没有匹配 '{sys.argv[2]}' 的 skill")
for s in hits:
    print(f"{r['categories'][s['category']]['emoji']} [{s['category']}] {s['name']}")
    print(f"   {s['description'][:120]}\n")
print(f"共 {len(hits)} 个匹配")
PY
}

# 用 sparse checkout 只拉一个 skill 目录，避免整仓 clone
_download() {   # <path> <目标目录>
  local path="$1" dest="$2" tmp
  tmp="$(mktemp -d)"
  git clone --depth 1 --filter=blob:none --sparse \
    "https://github.com/$REPO.git" "$tmp/r" > /dev/null 2>&1
  ( cd "$tmp/r" && git sparse-checkout set "$path" > /dev/null 2>&1 )
  [ -d "$tmp/r/$path" ] || { echo "仓库里没有 $path" >&2; rm -rf "$tmp"; exit 2; }
  rm -rf "$dest"; mkdir -p "$(dirname "$dest")"
  cp -R "$tmp/r/$path" "$dest"
  rm -rf "$tmp"
}

cmd_install() {
  local name="${1:?用法: skill-repo.sh install <name> [目标目录]}"
  local target="${2:-$SKILLS_DIR/$name}"
  fetch_registry
  local path; path="$(resolve "$name")"
  # 已存在则先备份——覆盖用户本地改动是不可逆的
  if [ -d "$target" ]; then
    local bak="${target}.bak.$(date +%Y%m%d%H%M%S)"
    mv "$target" "$bak"; echo "已备份原有版本 -> $bak"
  fi
  _download "$path" "$target"
  echo "✅ 已安装 $name -> $target"
  [ -f "$target/SKILL.md" ] && echo "   $(head -3 "$target/SKILL.md" | tail -1)"
}

cmd_update() {
  local name="${1:?用法: skill-repo.sh update <name>}"
  [ -d "$SKILLS_DIR/$name" ] || { echo "本地未安装 $name，请先 install" >&2; exit 2; }
  cmd_install "$name"
}

cmd_uninstall() {
  local name="${1:?用法: skill-repo.sh uninstall <name>}"
  local target="$SKILLS_DIR/$name"
  [ -d "$target" ] || { echo "本地未安装 $name" >&2; exit 2; }
  local bak="${target}.removed.$(date +%Y%m%d%H%M%S)"
  mv "$target" "$bak"
  echo "✅ 已从本地移除 $name（备份在 $bak，确认无用后自行删除）"
  echo "   仓库中的版本未受影响。要从仓库删除请用 remove。"
}

# upload / remove 都改仓库内容，一律走分支 + PR，不直推 main
_with_clone() {   # <分支名> <提交信息> <操作函数>
  local branch="$1" msg="$2" fn="$3" tmp
  tmp="$(mktemp -d)"
  git clone --depth 1 "https://github.com/$REPO.git" "$tmp/r" > /dev/null 2>&1
  ( cd "$tmp/r" && git checkout -q -b "$branch" && "$fn" "$tmp/r" \
    && python3 tools/build-registry.py > /dev/null \
    && git add -A && git commit -q -m "$msg" && git push -q -u origin "$branch" )
  ( cd "$tmp/r" && gh pr create --title "$msg" --body "由 skill-repo.sh 自动创建。" 2>&1 | tail -1 )
  rm -rf "$tmp"
}

cmd_upload() {
  local src="${1:?用法: skill-repo.sh upload <本地目录> <分类>}"
  local cat="${2:?缺少分类。可用分类见 skill-repo.sh list}"
  [ -f "$src/SKILL.md" ] || { echo "$src 下没有 SKILL.md，不是一个 skill" >&2; exit 2; }
  local name; name="$(basename "$src")"
  fetch_registry
  python3 - "$CACHE" "$cat" "$name" <<'PY'
import json, sys
r = json.load(open(sys.argv[1])); cat, name = sys.argv[2], sys.argv[3]
if cat not in r['categories']:
    sys.exit(f"未知分类 '{cat}'；可选：{', '.join(sorted(r['categories']))}")
dup = [s for s in r['skills'] if s['name'] == name]
if dup:
    sys.exit(f"'{name}' 已存在于 {dup[0]['category']}。要覆盖请先 remove，或换个名字。")
PY
  _upload_op() { rm -rf "$1/$cat/$name"; mkdir -p "$1/$cat"
                 rsync -a --exclude='.git' --exclude='*.log' "$src/" "$1/$cat/$name/"; }
  _with_clone "add-$name" "feat($cat): 新增 skill $name" _upload_op
}

cmd_remove() {
  local name="${1:?用法: skill-repo.sh remove <name>}"
  fetch_registry
  local path; path="$(resolve "$name")"
  _remove_op() { rm -rf "${1:?}/$path"; }
  _with_clone "remove-$name" "chore: 移除 skill $name" _remove_op
}

case "${1:-}" in
  list)      shift; cmd_list "${1:-}" ;;
  search)    shift; cmd_search "${1:-}" ;;
  install)   shift; cmd_install "$@" ;;
  update)    shift; cmd_update "$@" ;;
  uninstall) shift; cmd_uninstall "$@" ;;
  upload)    shift; cmd_upload "$@" ;;
  remove)    shift; cmd_remove "$@" ;;
  *) sed -n '2,20p' "$0" | sed 's/^# \{0,1\}//' ;;
esac
