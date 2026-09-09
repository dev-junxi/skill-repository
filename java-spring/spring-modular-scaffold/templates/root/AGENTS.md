# __CLASS_PREFIX__ Server Java

__CLASS_PREFIX__ 的 Java 服务端。模块化单体：业务模块各自独立 Maven module，跨模块依赖只允许经由对方的 `-api`，为未来拆分做准备。

## Commands

```bash
# 所有 Maven 命令都要带这个前缀——本机默认 JDK 是 17
export JAVA_HOME=/opt/homebrew/opt/openjdk@21

mvn clean test                      # 全部测试（含 ArchUnit 与 Testcontainers 集成测试）
mvn install -DskipTests && mvn -pl server/__ARTIFACT_PREFIX__-app spring-boot:run   # 本地启动
mvn spotless:apply                  # 格式化
./tools/docs-index.sh               # 重新生成模块索引
./tools/module-graph.sh             # 重新生成依赖快照
# @if dao
./tools/check-migrations.sh         # 校验迁移版本号
# @endif
```

提交前必须全过：

```bash
mvn spotless:check
mvn test
./tools/docs-index.sh --check
./tools/module-graph.sh --check
# @if dao
./tools/check-migrations.sh
# @endif
```

# @if dao
集成测试需要 Docker 在运行。
# @endif
启动 app **不能用 `-am`**——那会让 `spring-boot:run` 也作用到根聚合 pom。

## 架构铁律

- **跨模块依赖只经 `-api` module**。`X-web` 绝不能依赖 `Y-web` — ArchUnit 拦
- **domain 层零框架依赖**。不得出现 Spring / MyBatis 等框架的任何类型 — ArchUnit 拦
- **`@Transactional` 只写在 application 层** — ArchUnit 拦
<!-- @if auth -->
- **Controller 用 `@CurrentUser RequestUser`** 获取当前用户，不得直接调 `StpUtil` — ArchUnit 拦
<!-- @endif -->
- **业务代码禁止打印请求体/响应体**，`__ARTIFACT_PREFIX__-web-spring-boot-starter` 已统一记录并脱敏
- **应用层零状态**，架构必须支持单体多实例部署
<!-- @if dao -->
- **集成测试用 Testcontainers**，绝不连共享测试库
<!-- @endif -->

标了「ArchUnit 拦」的会被 `mvn test` 直接拦下，其余靠 review。

## Boundaries

- **Always**：改任何模块前先读它目录下的 `AGENTS.md`
- **Ask First**：加新的第三方依赖、改数据库 schema、引入 Redis 或任何新中间件
- **Never**：提交密钥到 yml、手改 `AGENTS.md` 的索引区块、手改 `docs/architecture/module-graph.txt`、改动已发布的 Flyway 迁移文件

## 新增模块时必做的四件事

漏掉任何一件 CI 都会红：

1. 加进根 `pom.xml` 的 `<modules>` 与 `<dependencyManagement>`
2. 加进 `tools/__ARTIFACT_PREFIX__-archunit/pom.xml` 的 `<dependencies>`——否则 ArchUnit 扫不到它，规则形同虚设
3. 写该模块的 `AGENTS.md`（frontmatter 必须有 `summary`）
4. 跑 `./tools/docs-index.sh && ./tools/module-graph.sh` 更新两个快照

## 模块索引

<!-- BEGIN:MODULE-INDEX -->
<!-- END:MODULE-INDEX -->

## 提交前的本地卡口

```bash
git config core.hooksPath tools/hooks   # 每台机器 clone 后跑一次
```

`tools/hooks/pre-push` 会在 push 前跑三个 shell 卡口（约 1 秒）。

⚠️ **这是 branch protection 的弱替代，不是等价物**：能被 `git push --no-verify` 绕过、只在配过的机器上生效、不跑测试。私有仓库启用 branch protection 需要 GitHub Pro；在那之前 CI 只能"报告"不能"拦截"。

## 决策记录

架构决策全部记在 `docs/adr/`，**append-only**：推翻旧决策的方式是新增一条并把旧条标 `Superseded`，不删不改。

遇到"为什么要这么做"的问题先查 ADR，那里有当时的取舍与代价。
