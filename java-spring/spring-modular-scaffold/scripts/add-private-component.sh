#!/usr/bin/env bash
# 把一个私有组件接进已生成的项目。只做接线，不做装配。
#
# 接线是机械的，漏了会**静默失效**（最典型：忘了加进 archunit pom，
# 所有架构规则对该模块形同虚设而测试仍全绿）。
# 装配是判断性的，漏了会**编译失败**——所以留给人。
#
# 全部参数走环境变量：
#   TARGET_DIR       项目根目录
#   PRIVATE_GROUP    私有 artifact 的 groupId
#   PRIVATE_ARTIFACT 私有 artifact 的 artifactId
#   PRIVATE_VERSION  版本号
#   PRIVATE_NAME     模块短名（如 view），小写
#   GROUP_ID / ARTIFACT_PREFIX / BASE_PACKAGE / PROJECT_NAME  同 scaffold.sh
set -euo pipefail

SKILL_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck source=lib.sh
source "$SKILL_DIR/scripts/lib.sh"

for v in TARGET_DIR PRIVATE_GROUP PRIVATE_ARTIFACT PRIVATE_VERSION PRIVATE_NAME \
         GROUP_ID ARTIFACT_PREFIX BASE_PACKAGE PROJECT_NAME; do
  [ -n "${!v:-}" ] || { echo "缺少必填变量: $v" >&2; exit 2; }
done

CLASS_PREFIX="$(derive_class_prefix "$ARTIFACT_PREFIX")"
BASE_PACKAGE_PATH="$(derive_package_path "$BASE_PACKAGE")"
ENV_PREFIX="$(derive_env_prefix "$ARTIFACT_PREFIX")"
PRIVATE_CLASS="$(derive_class_prefix "$PRIVATE_NAME")"
export CLASS_PREFIX BASE_PACKAGE_PATH ENV_PREFIX PRIVATE_CLASS
export GROUP_ID ARTIFACT_PREFIX BASE_PACKAGE PROJECT_NAME
export PRIVATE_GROUP PRIVATE_ARTIFACT PRIVATE_NAME

MOD="${ARTIFACT_PREFIX}-${PRIVATE_NAME}-spring-boot-starter"
MOD_DIR="$TARGET_DIR/components/$MOD"
# 幂等：在改任何 pom 之前就拒绝，避免重复插入
[ -e "$MOD_DIR" ] && { echo "模块已存在: $MOD_DIR" >&2; exit 2; }

# ── 1. 生成模块骨架
TPL="$SKILL_DIR/templates/components/private-starter"
while IFS= read -r -d '' f; do
  out="$(printf '%s' "$f" | substitute)"
  mkdir -p "$MOD_DIR/$(dirname "$out")"
  substitute < "$TPL/$f" > "$MOD_DIR/$out"
done < <(cd "$TPL" && find . -type f -print0)
echo "✅ 模块骨架: components/$MOD"

# ── 2~5. 四处 pom 接线
#
# 用 python 而非 sed：XML 的插入点需要结构感知，sed 在多个同名标签间容易插错位置。
insert_before() {   # <pom> <锚点字符串> <要插入的块> <幂等判据> <成功提示>
  python3 - "$1" "$2" "$3" "$4" "$5" <<'PY'
import sys, pathlib
pom, anchor, block, idem, msg = sys.argv[1:6]
p = pathlib.Path(pom)
if not p.is_file():
    print(f"  ⚠️  跳过（文件不存在）: {pom}"); sys.exit(0)
s = p.read_text()
if idem in s:
    print(f"  ○ 已存在，跳过: {msg}"); sys.exit(0)
if anchor not in s:
    print(f"  ❌ 找不到锚点，未能接线: {msg}", file=sys.stderr); sys.exit(1)
p.write_text(s.replace(anchor, block + anchor, 1))
print(f"  ✅ {msg}")
PY
}

ROOT_POM="$TARGET_DIR/pom.xml"
ARCH_POM="$TARGET_DIR/tools/${ARTIFACT_PREFIX}-archunit/pom.xml"
# parent 独立时版本声明写进 parent，否则写进根 pom。
#
# 判据必须是「根 pom 的 <parent> 指向谁」，不能是「文件系统里有没有同名目录」——
# 后者会把同级目录下别的项目留下的 <prefix>-parent 误当成自己的
# （实测踩过：monorepo 模式的项目把版本声明写进了另一个项目的 parent）。
PARENT_POM="$(dirname "$TARGET_DIR")/${ARTIFACT_PREFIX}-parent/pom.xml"
VERSION_POM="$ROOT_POM"
if grep -q "<artifactId>${ARTIFACT_PREFIX}-parent</artifactId>" "$ROOT_POM" 2>/dev/null; then
  if [ -f "$PARENT_POM" ]; then
    VERSION_POM="$PARENT_POM"
  else
    echo "  ⚠️  根 pom 声明 parent 为 ${ARTIFACT_PREFIX}-parent，但 $PARENT_POM 不存在" >&2
    echo "      版本声明改写进根 pom；若 parent 在别处，请手动同步" >&2
  fi
fi

echo "接线："

insert_before "$ROOT_POM" "    </modules>" \
  "        <module>components/$MOD</module>
" "components/$MOD</module>" "根 pom <modules>"

insert_before "$ROOT_POM" "        </dependencies>
    </dependencyManagement>" \
  "            <dependency>
                <groupId>$GROUP_ID</groupId>
                <artifactId>$MOD</artifactId>
                <version>\${project.version}</version>
            </dependency>
" "<artifactId>$MOD</artifactId>" "根 pom <dependencyManagement>"

insert_before "$VERSION_POM" "        </dependencies>
    </dependencyManagement>" \
  "            <dependency>
                <groupId>$PRIVATE_GROUP</groupId>
                <artifactId>$PRIVATE_ARTIFACT</artifactId>
                <version>$PRIVATE_VERSION</version>
            </dependency>
" "<artifactId>$PRIVATE_ARTIFACT</artifactId>" "$PRIVATE_ARTIFACT 版本声明（写入 $(basename "$(dirname "$VERSION_POM")")/pom.xml）"

# 这一处最不能漏：漏了 ArchUnit 扫不到该模块字节码，所有规则对它形同虚设而测试仍全绿
insert_before "$ARCH_POM" "    </dependencies>" \
  "        <dependency>
            <groupId>$GROUP_ID</groupId>
            <artifactId>$MOD</artifactId>
        </dependency>
" "<artifactId>$MOD</artifactId>" "archunit pom（漏了会让所有架构规则对本模块静默失效）"

# ── 6. 刷新两个快照
cd "$TARGET_DIR"
./tools/docs-index.sh   > /dev/null 2>&1 && echo "  ✅ 文档索引已刷新" \
  || echo "  ⚠️  索引刷新失败——多半是 AGENTS.md 的 summary 还是 TODO，填完再跑一次"
./tools/module-graph.sh > /dev/null 2>&1 && echo "  ✅ 依赖快照已刷新" \
  || echo "  ⚠️  快照刷新失败"

cat <<EOF

接线完成。接下来要你自己做（脚手架不做装配）：
  1. components/$MOD/src/main/java/${BASE_PACKAGE_PATH}/component/${PRIVATE_NAME}/${PRIVATE_CLASS}AutoConfiguration.java
     —— 声明 $PRIVATE_ARTIFACT 需要的 bean
  2. components/$MOD/AGENTS.md —— 填掉三处 TODO
     （summary 不填，文档卡口会红）
EOF
