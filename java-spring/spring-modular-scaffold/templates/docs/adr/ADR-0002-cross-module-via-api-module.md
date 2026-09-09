# ADR-0002: 跨模块依赖只经 -api module

Status: Accepted (2026-09-08)

Context:
  Java 的 package 拦不住 import，"模块间零依赖"这类约束必须靠编译期边界强制。
  参考实现（Rust）用 crate 边界达成，Java 需要等价手段。

Decision:
  一业务模块拆成 `-api`（对外契约）+ `-web`（实现）两个 Maven module。
  跨模块调用只允许依赖对方的 `-api`；`X-web` 绝不可依赖 `Y-web`。

  调用落点在**调用方 infrastructure 的 Adapter**：domain 定义自己的 Port，
  infrastructure 里的 Adapter 实现它并调用对方的 `-api`。app 不承担胶水。

Consequences:
  - MUST `X-api` 只能依赖 `__ARTIFACT_PREFIX__-contract`，不得依赖任何其他 `Y-api`
    （否则 api 之间会传染成网，等于没拆）
  - MUST 每个业务模块都建 `-api`，暂无契约时放 package-info.java 占位，
    保证"找 X 的对外接口去哪看"永远只有一个答案
  - ⚠️ 双向依赖（A-web→B-api 且 B-web→A-api）**Maven 与 ArchUnit 都抓不到**：
    前者因 api/web 是不同 artifact，后者因它们是不同包。靠 module-graph 快照
    diff 让它在 PR 里显形

Verify:
  ./tools/module-graph.sh --check
  mvn -pl tools/__ARTIFACT_PREFIX__-archunit test   # R1、R9
