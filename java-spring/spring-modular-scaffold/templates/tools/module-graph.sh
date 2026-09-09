#!/usr/bin/env bash
# 生成跨模块依赖快照。直接解析 pom，不跑 mvn dependency:tree——快且不需要完整构建。
#
#   ./tools/module-graph.sh          重新生成
#   ./tools/module-graph.sh --check  与快照不一致则 exit 1（CI 用）
#
# 存在理由：A-web → B-api 与 B-web → A-api 同时存在时，
#   - Maven 不认为是循环（api 与 web 是不同 artifact）
#   - ArchUnit 的包循环检测也抓不到（a.api 与 a.web 是不同包）
# 现有工具都看不见这种双向依赖，它能一路长到没人能拆。快照 diff 让每次
# 跨模块依赖变更都在 PR 里显形。
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SNAPSHOT="$ROOT/docs/architecture/module-graph.txt"
CHECK_MODE=false
[ "${1:-}" = "--check" ] && CHECK_MODE=true

generate() {
  python3 - "$ROOT" <<'PY'
import re
import sys
from pathlib import Path

root = Path(sys.argv[1])
GROUP = "__BASE_PACKAGE__"


def strip_parent(text):
    """去掉 <parent> 块——否则会把 parent 的 artifactId 误当成本模块的"""
    return re.sub(r"<parent>.*?</parent>", "", text, flags=re.S)


def own_artifact_id(text):
    m = re.search(r"<artifactId>\s*([^<]+?)\s*</artifactId>", text)
    return m.group(1) if m else None


def __ARTIFACT_PREFIX___deps(text):
    block = re.search(r"<dependencies>(.*?)</dependencies>", text, flags=re.S)
    if not block:
        return []
    found = []
    for dep in re.finditer(r"<dependency>(.*?)</dependency>", block.group(1), flags=re.S):
        body = dep.group(1)
        g = re.search(r"<groupId>\s*([^<]+?)\s*</groupId>", body)
        a = re.search(r"<artifactId>\s*([^<]+?)\s*</artifactId>", body)
        if g and a and g.group(1) == GROUP:
            found.append(a.group(1))
    return found


edges = set()
for area in ("components", "server", "tools"):
    area_dir = root / area
    if not area_dir.is_dir():
        continue
    for pom in sorted(area_dir.rglob("pom.xml")):
        text = strip_parent(pom.read_text(encoding="utf-8"))
        module = own_artifact_id(text)
        if not module:
            continue
        for dep in __ARTIFACT_PREFIX___deps(text):
            edges.add(f"{module} -> {dep}")

print("# 跨模块依赖快照，由 tools/module-graph.sh 生成，请勿手动编辑")
print("# 任何新增的跨模块依赖都会在这里显形——这是发现双向依赖的唯一手段")
print()
for edge in sorted(edges):
    print(edge)
PY
}

mkdir -p "$(dirname "$SNAPSHOT")"
NEW="$(generate)"

if $CHECK_MODE; then
  [ -f "$SNAPSHOT" ] || { echo "ERROR: 快照不存在，先运行 ./tools/module-graph.sh" >&2; exit 1; }
  if diff <(echo "$NEW") "$SNAPSHOT" >/dev/null; then
    echo "module-graph: 依赖关系与快照一致"
  else
    echo "ERROR: 跨模块依赖发生变化，请确认这是有意的，然后运行 ./tools/module-graph.sh 更新快照" >&2
    diff <(echo "$NEW") "$SNAPSHOT" >&2 || true
    exit 1
  fi
else
  echo "$NEW" > "$SNAPSHOT"
  echo "module-graph: 已更新 $SNAPSHOT"
fi
