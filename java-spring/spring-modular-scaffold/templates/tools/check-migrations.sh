#!/usr/bin/env bash
# Flyway 迁移版本号唯一性校验。
#
# 两重检查：
#   1. 工作区内没有重复版本号
#   2. 版本号没有和 git 全分支历史里的其它文件撞号
#
# 第 2 条针对"不同分支各自用了同一个版本号"——最难查的那种，
# 因为本地怎么看都是对的，合并或换分支后才炸。
#
# 兼容性：只用 POSIX 与 bash 3.2 特性（macOS 自带 bash 是 3.2，
# 没有 mapfile，也没有 declare -A 关联数组）。
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

status=0

FILES="$(find . -path "*/src/main/resources/db/migration/V*.sql" -not -path './.git/*' | sort)"

if [ -z "$FILES" ]; then
  echo "check-migrations: 尚无迁移文件，跳过"
  exit 0
fi

version_of() {
  base="$(basename "$1")"
  base="${base%%__*}"
  echo "${base#V}"
}

# ── 1. 工作区内重复
DUPES="$(
  echo "$FILES" | while IFS= read -r f; do
    [ -n "$f" ] && version_of "$f"
  done | sort | uniq -d
)"

if [ -n "$DUPES" ]; then
  echo "$DUPES" | while IFS= read -r v; do
    [ -n "$v" ] || continue
    echo "ERROR: 版本号 $v 被重复使用" >&2
    echo "$FILES" | while IFS= read -r f; do
      [ -n "$f" ] && [ "$(version_of "$f")" = "$v" ] && echo "         $f" >&2
    done
  done
  status=1
fi

# ── 2. 与 git 全分支历史撞号
if git rev-parse --git-dir >/dev/null 2>&1; then
  VERSIONS="$(echo "$FILES" | while IFS= read -r f; do
    [ -n "$f" ] && version_of "$f"
  done | sort -u)"

  for v in $VERSIONS; do
    current_base=""
    for f in $FILES; do
      if [ "$(version_of "$f")" = "$v" ]; then
        current_base="$(basename "$f")"
        break
      fi
    done
    HIST="$(git log --all --diff-filter=A --name-only --pretty=format: \
              -- "*/V${v}__*.sql" 2>/dev/null | sort -u || true)"
    for h in $HIST; do
      [ -n "$h" ] || continue
      if [ "$(basename "$h")" != "$current_base" ]; then
        echo "ERROR: 版本号 $v 在其它分支已被占用" >&2
        echo "         本地: $current_base" >&2
        echo "         历史: $h" >&2
        echo "       换一个未被占用的版本号，不要复用" >&2
        status=1
      fi
    done
  done
fi

if [ $status -eq 0 ]; then
  COUNT="$(echo "$FILES" | grep -c . || true)"
  echo "check-migrations: $COUNT 个迁移版本号均唯一"
fi
exit $status
