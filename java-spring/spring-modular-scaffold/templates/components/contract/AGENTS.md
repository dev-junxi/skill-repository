---
module: __ARTIFACT_PREFIX__-contract
type: component
summary: 跨模块契约——RequestUser / R / PageData / ErrorCode / BusinessException / DomainEvent，零 Spring 依赖
---

# __ARTIFACT_PREFIX__-contract

## 怎么引入

```xml
<dependency>
    <groupId>__GROUP_ID__</groupId>
    <artifactId>__ARTIFACT_PREFIX__-contract</artifactId>
</dependency>
```

所有业务模块都必须依赖它，无需配置。

## 边界

判据一句话：**能不能在一个没有 Spring 的 `main()` 里 new 出来直接用？** 能 → 放这里；不能 → 放对应的 starter。

零 Spring 是硬约束（ArchUnit R7），因为它是未来的跨服务协议包——RPC 客户端、契约生成工具等消费方不该被迫拖进整个 Spring Web。当前 compile scope 只有 `jackson-annotations` 一个依赖。

`DomainEvent` 刻意不继承 `ApplicationEvent`（ADR-0013），正是为了守住这条。

## 什么时候不该往这里加东西

- 需要 Spring 注解才能工作的类（Filter、Advice、AutoConfiguration）
- 只有一个模块用的类型——那属于该模块自己的 `-api`
- 任何带持久化语义的类型（PO、Mapper）
