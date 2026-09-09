---
module: __ARTIFACT_PREFIX__-archunit
type: tool
summary: 架构约束测试——10 条 ArchUnit 规则，每条都配违规 fixture 证明它会红
---

# __ARTIFACT_PREFIX__-archunit

## 铁律

**每新增一个 module，必须把它加进本 module 的 `pom.xml` 的 `<dependencies>`。** 否则 ArchUnit 扫不到它的字节码，所有规则对它形同虚设——而测试仍然全绿。

**每条规则必须配两个测试**：真实代码通过 + 违规 fixture 抛 `AssertionError`。只有前者的规则是危险的：一个匹配不到任何类的包表达式会让 `check()` 空集通过，永远绿，什么都没管住。

## 规则清单

定义在 `ArchRules.java`，编号与 spec §7 对应。实际可执行 10 条：R1-R7、R9、R11、R12。R8 是豁免（agentv2 不套 DDD，在导入时排除）；R10 与 R1 是同一条断言且 Maven 已在编译期硬挡。

## 为什么有些测试是 skipped

R1/R2/R3/R4/R9 针对业务模块的包结构（`..domain..`、`..controller..`、`..server.*.web..`），脚手架阶段这些包还不存在。

ArchUnit 默认 `failOnEmptyShould=true`，规则匹配到空集时**直接报错而非假绿**——这是它的正确行为，**不要用 `allowEmptyShould(true)` 绕过**。这里改用 JUnit 假设（`assumeTrue`）：包不存在时跳过，**第一个业务模块落地后自动生效**，不需要任何人记得回来打开开关。

写假设条件时注意精确度：判断 R1 不能只写 `.web`，那会命中 `__BASE_PACKAGE__.component.web`，而 R1 针对的是 `__BASE_PACKAGE__.server.<模块>.web`。
