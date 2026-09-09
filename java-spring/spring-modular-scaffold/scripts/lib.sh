#!/usr/bin/env bash
# scaffold.sh 与 add-private-component.sh 共用的函数。

# strip_blocks <开启的 flag 列表，空格分隔>
#
# 处理 @if/@endif 条件块。标记行本身永远丢弃；块内容仅在 flag 开启时保留。
# 支持嵌套：外层关闭时，内层不论开关一律丢弃。
#
# 注释前缀无关——只看行内是否出现 '@if <flag>' 或 '@endif'，
# 所以同一套标记在 XML / Java / shell / yaml / markdown 里都能用。
strip_blocks() {
  local enabled=" ${1:-} "
  awk -v enabled="$enabled" '
    /@if[ \t]+[a-z_]+/ {
      line = $0
      sub(/.*@if[ \t]+/, "", line)
      sub(/[^a-z_].*/, "", line)
      depth++
      # 前后加空格再查找，避免 "dao" 命中 "daox" 这类前缀误匹配
      closed[depth] = (index(enabled, " " line " ") == 0) ? 1 : 0
      suppressed = 0
      for (i = 1; i <= depth; i++) if (closed[i]) suppressed = 1
      next
    }
    /@endif/ {
      closed[depth] = 0
      depth--
      suppressed = 0
      for (i = 1; i <= depth; i++) if (closed[i]) suppressed = 1
      next
    }
    { if (!suppressed) print }
  '
}

# substitute —— 把占位符换成环境变量的值
#
# 顺序敏感：__BASE_PACKAGE_PATH__ 必须在 __BASE_PACKAGE__ 之前替换，
# 否则前者会先被后者匹配掉一半，剩下 'com.acme_PATH__' 这种残骸。
# 同理 __ENV_PREFIX__ 要在其它之前，因为它会出现在 __ENV_PREFIX___XXX 形态里。
substitute() {
  sed -e "s|__BASE_PACKAGE_PATH__|${BASE_PACKAGE_PATH}|g" \
      -e "s|__BASE_PACKAGE__|${BASE_PACKAGE}|g" \
      -e "s|__ENV_PREFIX__|${ENV_PREFIX}|g" \
      -e "s|__ARTIFACT_PREFIX__|${ARTIFACT_PREFIX}|g" \
      -e "s|__CLASS_PREFIX__|${CLASS_PREFIX}|g" \
      -e "s|__PROJECT_NAME__|${PROJECT_NAME}|g" \
      -e "s|__GROUP_ID__|${GROUP_ID}|g" \
      -e "s|__RELEASE_REPO_ID__|${RELEASE_REPO_ID:-}|g" \
      -e "s|__RELEASE_REPO_URL__|${RELEASE_REPO_URL:-}|g" \
      -e "s|__PRIVATE_GROUP__|${PRIVATE_GROUP:-}|g" \
      -e "s|__PRIVATE_ARTIFACT__|${PRIVATE_ARTIFACT:-}|g" \
      -e "s|__PRIVATE_CLASS__|${PRIVATE_CLASS:-}|g" \
      -e "s|__PRIVATE_NAME__|${PRIVATE_NAME:-}|g"
}

# derive_class_prefix <artifactPrefix> —— 首字母大写，用于 XxxApplication 这类类名
derive_class_prefix() {
  printf '%s' "$(printf '%s' "${1:0:1}" | tr '[:lower:]' '[:upper:]')${1:1}"
}

# derive_package_path <basePackage> —— 点换斜杠，用于目录
#
# 用 tr 而非 ${1//./\/}：后者里的 \/ 会被 bash 当成字面的反斜杠加斜杠，
# 产出 com\/acme 这种残骸。斜杠又是该展开式自身的分隔符，转义规则容易记错。
derive_package_path() {
  printf '%s' "$1" | tr '.' '/'
}

# derive_env_prefix <artifactPrefix> —— 全大写，用于环境变量名
derive_env_prefix() {
  printf '%s' "$1" | tr '[:lower:]' '[:upper:]'
}
