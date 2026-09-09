#!/usr/bin/env bash
# lib.sh 的单元测试。纯 bash，无框架。
set -uo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"
source ./lib.sh

pass=0; fail=0
check() {
  local name="$1" expected="$2" actual="$3"
  if [ "$expected" = "$actual" ]; then
    pass=$((pass + 1))
  else
    fail=$((fail + 1))
    echo "❌ $name"
    echo "   期望: $(printf '%q' "$expected")"
    echo "   实际: $(printf '%q' "$actual")"
  fi
}

# ── strip_blocks：flag 开启时保留内容、去掉标记行
out=$(printf 'a\n// @if auth\nb\n// @endif\nc\n' | strip_blocks "auth")
check "flag 开启保留内容" "$(printf 'a\nb\nc')" "$out"

# ── flag 关闭时连内容一起丢
out=$(printf 'a\n// @if auth\nb\n// @endif\nc\n' | strip_blocks "")
check "flag 关闭丢弃内容" "$(printf 'a\nc')" "$out"

# ── 多 flag：只开 dao
out=$(printf '1\n# @if auth\n2\n# @endif\n# @if dao\n3\n# @endif\n4\n' | strip_blocks "dao")
check "多 flag 只开 dao" "$(printf '1\n3\n4')" "$out"

# ── 注释前缀无关：XML 风格
out=$(printf 'x\n<!-- @if dao -->\ny\n<!-- @endif -->\nz\n' | strip_blocks "dao")
check "XML 注释前缀" "$(printf 'x\ny\nz')" "$out"

# ── 未声明的 flag 按关闭处理
out=$(printf 'a\n// @if unknown\nb\n// @endif\nc\n' | strip_blocks "auth dao")
check "未知 flag 按关闭" "$(printf 'a\nc')" "$out"

# ── 嵌套块：外层开、内层关
out=$(printf 'a\n# @if dao\nb\n# @if auth\nc\n# @endif\nd\n# @endif\ne\n' | strip_blocks "dao")
check "嵌套块内层关闭" "$(printf 'a\nb\nd\ne')" "$out"

# ── 嵌套块：外层关，内层不论开关都丢
out=$(printf 'a\n# @if dao\nb\n# @if auth\nc\n# @endif\nd\n# @endif\ne\n' | strip_blocks "auth")
check "嵌套块外层关闭" "$(printf 'a\ne')" "$out"

# ── 嵌套块：内外都开
out=$(printf 'a\n# @if dao\nb\n# @if auth\nc\n# @endif\nd\n# @endif\ne\n' | strip_blocks "auth dao")
check "嵌套块内外都开" "$(printf 'a\nb\nc\nd\ne')" "$out"

# ── 无标记的文件原样通过
out=$(printf 'hello\nworld\n' | strip_blocks "auth")
check "无标记原样通过" "$(printf 'hello\nworld')" "$out"

# ── flag 名前缀不能误匹配（dao 不应命中 daox）
out=$(printf 'a\n# @if daox\nb\n# @endif\nc\n' | strip_blocks "dao")
check "flag 名不做前缀匹配" "$(printf 'a\nc')" "$out"

# ── substitute：全部占位符替换
export GROUP_ID="com.acme" ARTIFACT_PREFIX="acme" BASE_PACKAGE="com.acme.demo" \
       PROJECT_NAME="acme-server" CLASS_PREFIX="Acme" BASE_PACKAGE_PATH="com/acme/demo" \
       ENV_PREFIX="ACME"
out=$(printf '__GROUP_ID__ __ARTIFACT_PREFIX__ __BASE_PACKAGE__ __PROJECT_NAME__ __CLASS_PREFIX__ __BASE_PACKAGE_PATH__\n' | substitute)
check "占位符全替换" "com.acme acme com.acme.demo acme-server Acme com/acme/demo" "$out"

# ── substitute：PATH 变体必须先于 BASE_PACKAGE 替换，否则留下残骸
out=$(printf '__BASE_PACKAGE_PATH__\n' | substitute)
check "PATH 变体不被 BASE_PACKAGE 吃掉" "com/acme/demo" "$out"

# ── substitute：同一行出现多次
out=$(printf '__ARTIFACT_PREFIX__-__ARTIFACT_PREFIX__\n' | substitute)
check "同行多次出现" "acme-acme" "$out"

# ── substitute：环境变量占位符
out=$(printf '__ENV_PREFIX___JWT_SECRET\n' | substitute)
check "环境变量占位符" "ACME_JWT_SECRET" "$out"

# ── derive_class_prefix
check "派生类名前缀" "Acme" "$(derive_class_prefix acme)"
check "派生类名前缀-单字母" "X" "$(derive_class_prefix x)"

# ── derive_package_path
check "派生包路径" "com/acme/core" "$(derive_package_path com.acme.core)"

# ── derive_env_prefix
check "派生环境变量前缀" "ACME" "$(derive_env_prefix acme)"

echo
echo "通过 $pass，失败 $fail"
[ $fail -eq 0 ]
