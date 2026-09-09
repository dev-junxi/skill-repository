---
module: __ARTIFACT_PREFIX__-dao-spring-boot-starter
type: component
summary: 持久层封装——BaseDao / BaseService / 条件构造 / 批量写，基于 MyBatis-Plus
---

# __ARTIFACT_PREFIX__-dao-spring-boot-starter

## 怎么引入

```xml
<dependency>
    <groupId>__GROUP_ID__</groupId>
    <artifactId>__ARTIFACT_PREFIX__-dao-spring-boot-starter</artifactId>
</dependency>
```

## 铁律

**domain 层不得依赖本组件**（ArchUnit R3）。Repository 接口定义在 domain，实现放 infrastructure，PO 与 domain 实体之间用 MapStruct 转换。domain 必须是能脱离框架直接 new 出来测试的纯 POJO。

**`@Transactional` 只写在 application 层**（ArchUnit R5）。

**跨模块的数据访问不允许**——只能经由对方的 `-api`（ADR-0002）。

## 集成测试

一律用 **Testcontainers**，**绝不连共享测试库**。

参考实现的前身为此付出过六次代价：共享库上的残留数据会让"查集合 + contains"型断言悄悄变成恒真、`--test-threads=1` 会掩盖并发缺陷、本地起的 mysqld 顶替端口会让"全绿"报告完全是假的。这些失效有个共同点——**不会被任何现有测试发现**。

`BaseDaoIntegrationTest` 里有一条断言 `jdbcUrl` 不含 `:3307/`，就是防止有人把它改回共享库。

## 移植时的五处适配（都是版本/依赖差异，非重构）

1. **`ServiceImpl` / `IService` 换包**：MyBatis-Plus 3.5.17 把它们从 `extension.service` 挪到了 `spring.service`（新的 `mybatis-plus-spring` artifact）
2. **自制 Optional → `java.util.Optional`**：来源用的是可序列化的自制 Optional。只补了 JDK 没有的 `ofEmpty`（空集合视同不存在），放在 `utils/DaoCollections`；`mapWith` 那一处改写成等价的 `map`
3. **`MslCollectionUtil` → `DaoCollections`**：只实现实际用到的 `join`/`split`/`toList` 三个方法，不整包搬运。**类名刻意不叫 `CollectionUtils`**——会与 commons-collections4 的同名类冲突
4. **`PageData` 改用 contract 里的不可变 record**：`BuilderUtils.buildView` 从"new 出来逐个 set"改为构造新实例
5. **切断 `msl-common` 依赖**：原本只用到 `base.PageData` 与 `base.Optional` 两个类型，均已替换

## 什么时候不该用它

- 只读的简单查询且不需要条件构造——直接写 Mapper 更清楚
