#!/usr/bin/env bash
# 从原型项目抽取模板并反向占位符化。一次性工具，原型升级时可重跑。
#
# 用法: extract-from-prototype.sh [原型目录]
set -euo pipefail

# 原型项目路径必须显式传入——这是维护者自用的工具，不该硬编码任何人的本地路径
PROTO="${1:?用法: extract-from-prototype.sh <原型项目目录>}"
DEST="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/templates"

[ -d "$PROTO" ] || { echo "原型不存在: $PROTO" >&2; exit 1; }

# 反向替换。
#
# ⚠️ 不能用 \b 词边界：macOS 的 BSD sed 不支持它，且是**静默失效**
#    （不报错，只是什么都不替换）。实测确认过。
#    改用严格的「长在前、短在后」排序达到等效效果：前几条消化掉
#    带限定的形态，剩下的 Lumos / lumos- / lumos 就是无歧义的。
placeholderize() {
  sed -e 's|com/lumos|__BASE_PACKAGE_PATH__|g' \
      -e 's|com\.lumos|__BASE_PACKAGE__|g' \
      -e 's|lumos-server-java|__PROJECT_NAME__|g' \
      -e 's|LUMOS_|__ENV_PREFIX___|g' \
      -e 's|Lumos|__CLASS_PREFIX__|g' \
      -e 's|lumos-|__ARTIFACT_PREFIX__-|g' \
      -e 's|lumos|__ARTIFACT_PREFIX__|g'
}

# copy_tree <相对原型的源目录> <相对 templates 的目标目录>
copy_tree() {
  local src="$PROTO/$1" dst="$DEST/$2"
  [ -d "$src" ] || { echo "跳过（不存在）: $1"; return 0; }
  mkdir -p "$dst"
  local count=0
  while IFS= read -r -d '' f; do
    # 路径本身也要占位符化：com/lumos/... 与 lumos-xxx 目录名
    local out
    out="$(printf '%s' "$f" | placeholderize)"
    mkdir -p "$dst/$(dirname "$out")"
    placeholderize < "$src/$f" > "$dst/$out"
    count=$((count + 1))
  done < <(cd "$src" && find . -type f -not -path './target/*' -print0)
  echo "抽取: $1 -> templates/$2 ($count 文件)"
}

copy_file() {
  local src="$PROTO/$1" dst="$DEST/$2"
  [ -f "$src" ] || { echo "跳过（不存在）: $1"; return 0; }
  mkdir -p "$(dirname "$dst")"
  placeholderize < "$src" > "$dst"
  echo "抽取: $1 -> templates/$2"
}

echo "=== 恒定模板 ==="
copy_file pom.xml                root/pom.xml
copy_file AGENTS.md              root/AGENTS.md
copy_file CLAUDE.md              root/CLAUDE.md
copy_file .gitignore             root/.gitignore
copy_tree components/lumos-contract                components/contract
copy_tree components/lumos-web-spring-boot-starter components/web-starter
copy_tree server/lumos-app                         server/app
copy_tree tools/lumos-archunit                     tools/archunit
copy_file tools/docs-index.sh    tools/docs-index.sh
copy_file tools/module-graph.sh  tools/module-graph.sh
copy_file tools/hooks/pre-push   tools/hooks/pre-push
copy_file docs/architecture/module-dependency-rules.md docs/architecture/module-dependency-rules.md
copy_file docs/ops/ci-and-guards.md                    docs/ops/ci-and-guards.md
copy_file docs/ops/toolchain.md                        docs/ops/toolchain.md
copy_file .github/workflows/ci.yml            ci/ci.yml
copy_file .github/workflows/security-scan.yml ci/security-scan.yml

echo "=== 条件模板 ==="
copy_tree components/lumos-auth-spring-boot-starter components/auth-starter
copy_tree components/lumos-dao-spring-boot-starter  components/dao-starter
copy_file tools/check-migrations.sh tools/check-migrations.sh

echo "=== ADR（排除 0005 加密与 0008 Boot 版本）==="
for f in "$PROTO"/docs/adr/ADR-*.md; do
  b="$(basename "$f")"
  case "$b" in
    ADR-0005-*|ADR-0008-*) echo "排除（Lumos 特定）: $b"; continue ;;
  esac
  placeholderize < "$f" > "$DEST/docs/adr/$b"
  echo "抽取 ADR: $b"
done

echo
echo "完成。下一步：手工加条件块标记（见 Task 2）"
