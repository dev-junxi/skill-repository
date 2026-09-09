#!/usr/bin/env bash
# 按配置从 templates/ 生成项目骨架。
#
# 全部参数走环境变量：
#   GROUP_ID / ARTIFACT_PREFIX / BASE_PACKAGE / PROJECT_NAME  必填
#   TARGET_DIR                                                必填，生成到哪
#   OPT_AUTH / OPT_DAO      = yes|no        默认 no
#   OPT_PARENT              = standalone|monorepo   默认 monorepo
#   RELEASE_REPO_ID / RELEASE_REPO_URL      OPT_PARENT=standalone 时必填
set -euo pipefail

SKILL_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TPL="$SKILL_DIR/templates"
# shellcheck source=lib.sh
source "$SKILL_DIR/scripts/lib.sh"

# ── 必填校验：早失败好过生成一半
for v in GROUP_ID ARTIFACT_PREFIX BASE_PACKAGE PROJECT_NAME TARGET_DIR; do
  [ -n "${!v:-}" ] || { echo "缺少必填变量: $v" >&2; exit 2; }
done

OPT_AUTH="${OPT_AUTH:-no}"
OPT_DAO="${OPT_DAO:-no}"
OPT_PARENT="${OPT_PARENT:-monorepo}"

case "$OPT_PARENT" in
  standalone)
    for v in RELEASE_REPO_ID RELEASE_REPO_URL; do
      [ -n "${!v:-}" ] || { echo "OPT_PARENT=standalone 需要: $v" >&2; exit 2; }
    done ;;
  monorepo) ;;
  *) echo "OPT_PARENT 只能是 standalone 或 monorepo，收到: $OPT_PARENT" >&2; exit 2 ;;
esac

# 生成器误覆盖一个已有项目是不可逆的破坏
[ -e "$TARGET_DIR" ] && { echo "目标已存在，拒绝覆盖: $TARGET_DIR" >&2; exit 2; }

CLASS_PREFIX="$(derive_class_prefix "$ARTIFACT_PREFIX")"
BASE_PACKAGE_PATH="$(derive_package_path "$BASE_PACKAGE")"
ENV_PREFIX="$(derive_env_prefix "$ARTIFACT_PREFIX")"
export CLASS_PREFIX BASE_PACKAGE_PATH ENV_PREFIX
export GROUP_ID ARTIFACT_PREFIX BASE_PACKAGE PROJECT_NAME
export RELEASE_REPO_ID="${RELEASE_REPO_ID:-}" RELEASE_REPO_URL="${RELEASE_REPO_URL:-}"

FLAGS=""
[ "$OPT_AUTH" = yes ] && FLAGS="$FLAGS auth"
[ "$OPT_DAO" = yes ] && FLAGS="$FLAGS dao"
case "$OPT_PARENT" in
  standalone) FLAGS="$FLAGS standalone_parent" ;;
  monorepo)   FLAGS="$FLAGS monorepo_parent" ;;
esac
echo "启用的 flag:$FLAGS"

# render <模板文件> <目标文件>
render() {
  local src="$1" dst="$2"
  mkdir -p "$(dirname "$dst")"
  strip_blocks "$FLAGS" < "$src" | substitute > "$dst"
  [ -x "$src" ] && chmod +x "$dst"
  return 0
}

# render_tree <模板目录> <目标目录>
render_tree() {
  local src="$1" dst="$2" f out
  [ -d "$src" ] || return 0
  while IFS= read -r -d '' f; do
    out="$(printf '%s' "$f" | substitute)"   # 路径里的占位符也要替换
    render "$src/$f" "$dst/$out"
  done < <(cd "$src" && find . -type f -print0)
}

mkdir -p "$TARGET_DIR"

echo "=== 根文件 ==="
render_tree "$TPL/root" "$TARGET_DIR"

echo "=== 恒定组件 ==="
render_tree "$TPL/components/contract"    "$TARGET_DIR/components/${ARTIFACT_PREFIX}-contract"
render_tree "$TPL/components/web-starter" "$TARGET_DIR/components/${ARTIFACT_PREFIX}-web-spring-boot-starter"
render_tree "$TPL/server/app"             "$TARGET_DIR/server/${ARTIFACT_PREFIX}-app"
render_tree "$TPL/tools/archunit"         "$TARGET_DIR/tools/${ARTIFACT_PREFIX}-archunit"

echo "=== 卡口脚本 ==="
render "$TPL/tools/docs-index.sh"   "$TARGET_DIR/tools/docs-index.sh"
render "$TPL/tools/module-graph.sh" "$TARGET_DIR/tools/module-graph.sh"
render "$TPL/tools/hooks/pre-push"  "$TARGET_DIR/tools/hooks/pre-push"
chmod +x "$TARGET_DIR/tools/docs-index.sh" "$TARGET_DIR/tools/module-graph.sh" \
         "$TARGET_DIR/tools/hooks/pre-push"

echo "=== 文档 ==="
render_tree "$TPL/docs/architecture" "$TARGET_DIR/docs/architecture"
render_tree "$TPL/docs/ops"          "$TARGET_DIR/docs/ops"
render "$TPL/docs/adr/README.md"     "$TARGET_DIR/docs/adr/README.md"

echo "=== ADR（按 manifest 裁剪）==="
while IFS=: read -r flag file; do
  [ -n "$flag" ] || continue
  case "$flag" in
    always) ;;
    auth) [ "$OPT_AUTH" = yes ] || continue ;;
    dao)  [ "$OPT_DAO" = yes ] || continue ;;
    *) echo "adr-manifest.txt 里有未知 flag: $flag" >&2; exit 2 ;;
  esac
  render "$TPL/docs/adr/$file" "$TARGET_DIR/docs/adr/$file"
  echo "  + $file"
done < "$TPL/adr-manifest.txt"

echo "=== CI ==="
render "$TPL/ci/ci.yml"            "$TARGET_DIR/.github/workflows/ci.yml"
render "$TPL/ci/security-scan.yml" "$TARGET_DIR/.github/workflows/security-scan.yml"

if [ "$OPT_AUTH" = yes ]; then
  echo "=== 鉴权组件 ==="
  render_tree "$TPL/components/auth-starter" "$TARGET_DIR/components/${ARTIFACT_PREFIX}-auth-spring-boot-starter"
  # satokenleak fixture 是整文件取舍：它 import cn.dev33.satoken，无鉴权时依赖不存在
  render_tree "$TPL/tools/archunit-auth-fixture" "$TARGET_DIR/tools/${ARTIFACT_PREFIX}-archunit"
fi

if [ "$OPT_DAO" = yes ]; then
  echo "=== 持久层组件 ==="
  render_tree "$TPL/components/dao-starter" "$TARGET_DIR/components/${ARTIFACT_PREFIX}-dao-spring-boot-starter"
  render "$TPL/tools/check-migrations.sh" "$TARGET_DIR/tools/check-migrations.sh"
  chmod +x "$TARGET_DIR/tools/check-migrations.sh"
fi

if [ "$OPT_PARENT" = standalone ]; then
  echo "=== 独立 parent（生成到 TARGET_DIR 的同级目录）==="
  PARENT_DIR="$(dirname "$TARGET_DIR")/${ARTIFACT_PREFIX}-parent"
  [ -e "$PARENT_DIR" ] && { echo "parent 目录已存在: $PARENT_DIR" >&2; exit 2; }
  render "$TPL/parent-standalone/pom.xml" "$PARENT_DIR/pom.xml"
  echo "  parent 生成于: $PARENT_DIR"
  echo "  ⚠️  需先在该目录 mvn install（或 deploy），根 pom 才能解析到它"
fi

echo "=== 生成索引与依赖快照 ==="
# 索引是被推导的、不是被维护的——所以模板里那个块是空的，由这里现场生成。
# 不做这一步，生成出来的项目第一次跑 docs-index.sh --check 就是红的。
( cd "$TARGET_DIR" && ./tools/docs-index.sh > /dev/null 2>&1 ) \
  && echo "  ✅ 模块索引已生成" || echo "  ⚠️  索引生成失败，请手动跑 ./tools/docs-index.sh"
( cd "$TARGET_DIR" && ./tools/module-graph.sh > /dev/null 2>&1 ) \
  && echo "  ✅ 依赖快照已生成" || echo "  ⚠️  快照生成失败，请手动跑 ./tools/module-graph.sh"

echo
echo "生成完毕: $TARGET_DIR"
echo "下一步：跑 scripts/verify.sh $TARGET_DIR 验收"
