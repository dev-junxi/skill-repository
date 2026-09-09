#!/usr/bin/env bash
# 校验每个 module 都有 AGENTS.md 且带 summary，并生成根 AGENTS.md 的索引区块。
#
# 索引是被推导的、不是被维护的，所以它不可能漂。
#   ./tools/docs-index.sh          重新生成
#   ./tools/docs-index.sh --check  与现状不一致则 exit 1（CI 用）
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET="$ROOT/AGENTS.md"
BEGIN_MARK='<!-- BEGIN:MODULE-INDEX -->'
END_MARK='<!-- END:MODULE-INDEX -->'
CHECK_MODE=false
[[ "${1:-}" == "--check" ]] && CHECK_MODE=true

fail() { echo "ERROR: $*" >&2; exit 1; }

frontmatter_field() {
  awk -v key="$2" '
    NR==1 && $0 != "---" { exit 1 }
    NR>1 && $0 == "---" { exit }
    NR>1 {
      if (index($0, key ":") == 1) {
        sub("^" key ":[[:space:]]*", "")
        print
        exit
      }
    }
  ' "$1"
}

scan_dir() {
  local kind="$1" dir="$2"
  [[ -d "$ROOT/$dir" ]] || return 0
  local entry name doc summary
  for entry in "$ROOT/$dir"/*/; do
    [[ -d "$entry" ]] || continue
    # 模块的定义是"有 pom.xml"。tools/ 下还放着 hooks 这类非模块目录，
    # 它们不该被要求写 AGENTS.md
    [[ -f "${entry}pom.xml" ]] || continue
    name="$(basename "$entry")"
    doc="${entry}AGENTS.md"
    [[ -f "$doc" ]] || fail "$dir/$name 缺少 AGENTS.md"
    summary="$(frontmatter_field "$doc" summary || true)"
    [[ -n "$summary" ]] || fail "$dir/$name/AGENTS.md 的 frontmatter 缺少 summary 字段"
    printf '| [%s](%s/%s/AGENTS.md) | %s | %s |\n' "$name" "$dir" "$name" "$kind" "$summary"
  done
}

generate_index() {
  echo "$BEGIN_MARK"
  echo '<!-- 本区块由 tools/docs-index.sh 生成，请勿手动编辑 -->'
  echo
  echo '| 模块 | 类型 | 说明 |'
  echo '|------|------|------|'
  scan_dir component components
  scan_dir server server
  scan_dir tool tools
  echo
  echo "$END_MARK"
}

[[ -f "$TARGET" ]] || fail "根 AGENTS.md 不存在"
grep -qF "$BEGIN_MARK" "$TARGET" || fail "根 AGENTS.md 缺少 $BEGIN_MARK 标记"
grep -qF "$END_MARK" "$TARGET" || fail "根 AGENTS.md 缺少 $END_MARK 标记"

# 用行号切分而非 awk -v：BSD awk 的 -v 不接受多行值
BEGIN_LINE="$(grep -nF "$BEGIN_MARK" "$TARGET" | head -1 | cut -d: -f1)"
END_LINE="$(grep -nF "$END_MARK" "$TARGET" | head -1 | cut -d: -f1)"
[[ -n "$BEGIN_LINE" && -n "$END_LINE" && "$BEGIN_LINE" -lt "$END_LINE" ]] \
  || fail "AGENTS.md 的索引标记顺序不对（BEGIN 必须在 END 之前）"

TMP="$(mktemp)"
head -n "$((BEGIN_LINE - 1))" "$TARGET" > "$TMP"
generate_index >> "$TMP"
tail -n "+$((END_LINE + 1))" "$TARGET" >> "$TMP"

if $CHECK_MODE; then
  if diff -q "$TARGET" "$TMP" >/dev/null; then
    echo "docs-index: 索引与模块结构一致"
    rm -f "$TMP"
  else
    echo "ERROR: 索引已过期，请运行 ./tools/docs-index.sh 重新生成" >&2
    diff "$TARGET" "$TMP" >&2 || true
    rm -f "$TMP"
    exit 1
  fi
else
  mv "$TMP" "$TARGET"
  echo "docs-index: 已更新 $TARGET"
fi
