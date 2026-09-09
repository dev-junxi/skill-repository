---
name: spring-modular-scaffold
description: Use when starting a new Spring Boot service and you want a modular architecture whose boundaries are enforced by the build rather than by convention — generates a buildable skeleton with layered modules, ArchUnit rules, documentation guards, and ADRs, with options for auth / persistence / parent distribution.
---

# Spring 模块化脚手架

生成一个 Spring Boot 项目骨架，其架构边界由**构建期卡口**强制，而非靠约定和自觉。

**产出**：`mvn test` 全绿、app 能启动、三个卡口脚本可用**且已被证明会在违规时报错**。

## 什么时候用

- 新起一个 Spring Boot 服务，希望模块边界从第一天就被强制
- 项目预期会长到多个业务模块，将来可能拆服务

## 什么时候不用

- 单文件小工具、demo、一次性脚本 —— 这套结构的开销远大于收益
- 已有项目要改造 —— 本 skill 只生成新项目
- 非 JVM 项目 —— 模板是 Maven + Spring 专用

## 流程

### 1. 收集参数

**四个基本信息**（无默认值，必须问）：

| 参数 | 说明 | 示例 |
|---|---|---|
| `GROUP_ID` | Maven groupId | `com.acme` |
| `ARTIFACT_PREFIX` | 模块名前缀，小写单词 | `acme` |
| `BASE_PACKAGE` | Java 根包 | `com.acme` |
| `PROJECT_NAME` | 项目名 / 根 artifactId | `acme-server` |

类名前缀、包目录、环境变量前缀都从这四个派生，不再多问。

**三个取舍**。每个都要说清代价，不要只列选项名：

**鉴权** `OPT_AUTH=yes|no`
- `yes`：Sa-Token JWT Stateless + `@CurrentUser` 注入。服务端不为鉴权查库，多实例无需共享存储；**代价是没有强制下线能力**，靠短 access TTL 替代
- `no`：不生成。内部服务、定时任务类项目适用

**持久层** `OPT_DAO=yes|no`
- `yes`：MyBatis-Plus 封装 + Testcontainers 集成测试基线 + 迁移版本号卡口
- `no`：不生成。纯计算服务、网关类项目适用
- ⚠️ **选 yes 有个前置**：dao 模块依赖私有 artifact `com.lumos:model-view-builder`（`BaseDaoImpl` 的泛型约束用了它的 `HasKey`，是结构性依赖）。**拉不到该 artifact 的机器上 dao 模块会构建失败**。没有它就选 `no`

**parent 分发** `OPT_PARENT=standalone|monorepo`
- `standalone`：parent 独立成 repo 发私服。**只有多个项目要共享同一套版本基线时才值得**，否则是纯开销。选它还要问 `RELEASE_REPO_ID` 与 `RELEASE_REPO_URL`
- `monorepo`：parent 内容并入根 pom。零基础设施成本

**最后问**：有没有需要接入的私有组件？（可多个）有的话收集 `PRIVATE_GROUP` / `PRIVATE_ARTIFACT` / `PRIVATE_VERSION` / `PRIVATE_NAME`。

### 2. 生成

```bash
GROUP_ID=... ARTIFACT_PREFIX=... BASE_PACKAGE=... PROJECT_NAME=... \
TARGET_DIR=... OPT_AUTH=... OPT_DAO=... OPT_PARENT=... \
~/.claude/skills/spring-modular-scaffold/scripts/scaffold.sh
```

`standalone` 模式会在 `TARGET_DIR` 的同级目录生成 parent，**必须先 `mvn install` 它**，根 pom 才解析得到。

每个私有组件再跑一次：

```bash
TARGET_DIR=... GROUP_ID=... ARTIFACT_PREFIX=... BASE_PACKAGE=... PROJECT_NAME=... \
PRIVATE_GROUP=... PRIVATE_ARTIFACT=... PRIVATE_VERSION=... PRIVATE_NAME=... \
~/.claude/skills/spring-modular-scaffold/scripts/add-private-component.sh
```

### 3. 验收 —— 不通过不算完成

```bash
MVN_CMD="env JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn" \
  ~/.claude/skills/spring-modular-scaffold/scripts/verify.sh <TARGET_DIR>
```

六条：`mvn test` · 启动冒烟 · 三个卡口**各自被证明会红** · ArchUnit 扫到真实代码。

预期：无持久层 `通过 5 · 跳过 1`；全选 `通过 6 · 跳过 0`。

**verify 不通过时不要把项目交付给用户**，先修。若因环境原因跑不了（选了持久层但没装 Docker），**如实说明哪几条未验证**，不要含糊带过。

### 4. 交付说明

告诉用户这几件事：

- **六条铁律不可关闭**，它们互相咬合（细节见 `references/architecture-rationale.md`）
- **新增模块必做四件事**：加进根 pom、**加进 archunit pom**、写 `AGENTS.md`、刷新两个快照。第二件漏了会让规则对新模块**静默失效而测试仍全绿**
- ADR 里凡涉及业务模型的（最典型是 token claim）**必须按自己的情况重写**，照抄是假的判例
- Spring Boot 版本写死在模板里，**上线前复核它是否仍在支持期**

## 边界

**做**：生成骨架、接线（pom / 模块注册 / 架构测试依赖）、生成文档基座与 ADR、验收卡口。

**不做**：生成业务模块 · 装配私有组件的 bean（只留空壳 + TODO）· 配置 CI 凭证 · 改造存量项目。

## 出问题时

读 `references/architecture-rationale.md` —— 每条铁律的来由和它防的具体事故都在那儿。**不要凭直觉调整结构**：这套约束里有几条看起来冗余，实际是互相兜底的。
