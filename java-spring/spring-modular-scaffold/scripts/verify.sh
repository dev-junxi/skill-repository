#!/usr/bin/env bash
# 验收生成出来的骨架。不通过则这次生成不算完成。
#
# 用法: verify.sh <生成的项目目录>
# 可选: MVN_CMD="env JAVA_HOME=... mvn"   自定义 maven 调用
#       KEEP_LOGS=1                      保留日志目录
#       APP_PORT=8080                    健康端点端口
#
# 纪律：一律 `cmd > log 2>&1; echo $?` 取真实退出码再 grep 日志。
# 禁止对管道取 $?——那取到的是管道最后一个命令的状态，恒为 0，
# 会造出一个永远绿的验证。
set -uo pipefail

TARGET="${1:?用法: verify.sh <项目目录>}"
cd "$TARGET" || exit 2
LOG_DIR="$(mktemp -d)"
MVN="${MVN_CMD:-mvn}"
APP_PORT="${APP_PORT:-8080}"
pass=0; fail=0; skip=0

ok()   { pass=$((pass + 1)); echo "✅ $1"; }
bad()  { fail=$((fail + 1)); echo "❌ $1"; [ -n "${2:-}" ] && [ -f "$2" ] && sed -n '1,20p' "$2"; }
warn() { skip=$((skip + 1)); echo "⚠️  $1"; }

has_dao=$([ -f tools/check-migrations.sh ] && echo yes || echo no)
APP_MODULE="$(find server -maxdepth 1 -mindepth 1 -type d | head -1)"
ARCH_MODULE="$(find tools -maxdepth 1 -mindepth 1 -type d -name '*archunit*' | head -1)"

echo "验收目标: $TARGET"
echo "  app 模块: $APP_MODULE   archunit 模块: $ARCH_MODULE   持久层: $has_dao"
echo

# ── 1/6 mvn test
echo "── 1/6 mvn test"
$MVN -q test > "$LOG_DIR/test.log" 2>&1
rc=$?
if [ $rc -eq 0 ]; then ok "mvn test 全绿"
else bad "mvn test 失败（退出码 $rc）" "$LOG_DIR/test.log"; fi

# ── 2/6 启动冒烟
echo "── 2/6 启动冒烟"
# 先装进本地仓库，否则 spring-boot:run 解析不到兄弟模块
$MVN -q install -DskipTests > "$LOG_DIR/install.log" 2>&1
$MVN -q -pl "$APP_MODULE" spring-boot:run > "$LOG_DIR/run.log" 2>&1 &
APP_PID=$!
health=""
for _ in $(seq 1 40); do
  sleep 2
  kill -0 $APP_PID 2>/dev/null || break     # 进程已退出，不必再等
  health="$(curl -s -m 3 -D "$LOG_DIR/head.txt" "http://localhost:${APP_PORT}/v1/health" 2>/dev/null || true)"
  [ -n "$health" ] && break
done
if printf '%s' "$health" | grep -q '"code":0' && grep -qi 'x-trace-id' "$LOG_DIR/head.txt" 2>/dev/null; then
  ok "健康端点返回统一响应体且带 x-trace-id"
else
  bad "启动冒烟失败（响应=${health:-空}）" "$LOG_DIR/run.log"
fi
kill $APP_PID 2>/dev/null
wait $APP_PID 2>/dev/null

# ── 3/6 docs-index 必须能红
echo "── 3/6 docs-index 卡口"
# 探针必须**长得像一个模块**：docs-index.sh 把「有 pom.xml」作为模块的定义
# （tools/hooks 那类非模块目录不该被要求写 AGENTS.md）。
# 空目录会被正确跳过，那样探针就试不出卡口。
mkdir -p components/__verify_probe
cat > components/__verify_probe/pom.xml <<'XML'
<?xml version="1.0" encoding="UTF-8"?>
<project><modelVersion>4.0.0</modelVersion>
  <groupId>verify.probe</groupId><artifactId>probe</artifactId><version>0</version>
</project>
XML
./tools/docs-index.sh --check > "$LOG_DIR/di.log" 2>&1
rc=$?
rm -rf components/__verify_probe
if [ $rc -ne 0 ]; then
  ./tools/docs-index.sh --check > "$LOG_DIR/di2.log" 2>&1
  if [ $? -eq 0 ]; then ok "docs-index 在缺 AGENTS.md 时报错，清理后恢复"
  else bad "docs-index 清理后仍不通过" "$LOG_DIR/di2.log"; fi
else
  bad "docs-index 对缺失 AGENTS.md 的模块没有报错——卡口是空转的" "$LOG_DIR/di.log"
fi

# ── 4/6 module-graph 必须能红
echo "── 4/6 module-graph 卡口"
./tools/module-graph.sh > /dev/null 2>&1
GRAPH=docs/architecture/module-graph.txt
if [ -f "$GRAPH" ]; then
  cp "$GRAPH" "$LOG_DIR/graph.bak"
  echo "probe-module -> probe-dep" >> "$GRAPH"
  ./tools/module-graph.sh --check > "$LOG_DIR/mg.log" 2>&1
  rc=$?
  cp "$LOG_DIR/graph.bak" "$GRAPH"
  if [ $rc -ne 0 ]; then ok "module-graph 在快照被改动时报错"
  else bad "module-graph 对快照改动没有报错——卡口是空转的" "$LOG_DIR/mg.log"; fi
else
  bad "module-graph 未生成快照文件 $GRAPH" "$LOG_DIR/mg.log"
fi

# ── 5/6 check-migrations 必须能红（仅有持久层时）
echo "── 5/6 check-migrations 卡口"
if [ "$has_dao" = yes ]; then
  D="$APP_MODULE/src/main/resources/db/migration"
  mkdir -p "$D"
  touch "$D/V209901010000__probe_a.sql" "$D/V209901010000__probe_b.sql"
  ./tools/check-migrations.sh > "$LOG_DIR/cm.log" 2>&1
  rc=$?
  rm -f "$D/V209901010000__probe_a.sql" "$D/V209901010000__probe_b.sql"
  rmdir -p "$D" 2>/dev/null
  if [ $rc -ne 0 ]; then ok "check-migrations 在版本号撞号时报错"
  else bad "check-migrations 对撞号没有报错——卡口是空转的" "$LOG_DIR/cm.log"; fi
else
  warn "跳过 check-migrations（本项目未启用持久层）"
fi

# ── 6/6 ArchUnit 必须扫到真实代码
#
# 这一条独立于 ArchUnit 自带的违规 fixture。fixture 只证明「规则在 fixture 上会红」，
# 不证明它扫到了真实代码——fixture 的包名是硬指定的，一个包匹配表达式写错的规则
# 在 fixture 上照样红，却对生产代码完全空转。两件事必须分开验证。
echo "── 6/6 ArchUnit 非空转"
# 探针必须放在**有 Spring 依赖**的模块里。
# 放 contract 是错的：它零 Spring 依赖（那是 R7 规则本身），
# 带 @Component 的类进去是**编译失败**而不是 ArchUnit 报错——
# 两者都让退出码非 0，但验证的不是我们声称的那件事。
WEB_AUTOCFG="$(find components -name '*WebAutoConfiguration.java' | head -1)"
if [ -z "$WEB_AUTOCFG" ]; then
  bad "找不到 web-starter 的 AutoConfiguration，无法定位探针位置"
else
  PKG_PATH="${WEB_AUTOCFG#*/src/main/java/}"
  PKG_PATH="$(dirname "$PKG_PATH")"
  WEB_MODULE="${WEB_AUTOCFG%%/src/main/java/*}"
  PROBE_DIR="$WEB_MODULE/src/main/java/$PKG_PATH/domain"
  mkdir -p "$PROBE_DIR"
  cat > "$PROBE_DIR/VerifyProbe.java" <<JAVA
package $(printf '%s' "$PKG_PATH" | tr '/' '.').domain;

import org.springframework.stereotype.Component;

@Component
public class VerifyProbe {}
JAVA
  # -am 不能省：没有它，archunit 会用本地仓库里装好的旧 web-starter，
  # 看不到刚写进去的探针类，于是永远报「空转」。
  $MVN -pl "$ARCH_MODULE" -am test > "$LOG_DIR/arch.log" 2>&1
  rc=$?
  rm -rf "$PROBE_DIR"
  # 只看退出码非 0 是不够的——构建可能因任何别的原因失败（编译错、依赖缺失、
  # 别的测试红）。必须确认失败信息里**点名了这个探针类**，才能断言
  # 「规则扫到了真实代码并拦下了它」。
  if [ $rc -ne 0 ] && grep -q "VerifyProbe" "$LOG_DIR/arch.log"; then
    ok "ArchUnit 拦下了 domain 包里的 Spring 注解——规则扫到了真实代码"
  elif [ $rc -ne 0 ]; then
    bad "archunit 模块构建失败，但失败信息里没有 VerifyProbe——不是被规则拦下的" "$LOG_DIR/arch.log"
  else
    bad "ArchUnit 对真实代码里的违规无反应——规则在空转" "$LOG_DIR/arch.log"
  fi
fi

echo
echo "════ 通过 $pass · 失败 $fail · 跳过 $skip ════"
if [ -n "${KEEP_LOGS:-}" ]; then echo "日志: $LOG_DIR"; else rm -rf "$LOG_DIR"; fi
[ $fail -eq 0 ]
