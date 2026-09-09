---
module: __ARTIFACT_PREFIX__-__PRIVATE_NAME__-spring-boot-starter
type: component
summary: TODO 一句话说明这个组件解决什么问题
---

# __ARTIFACT_PREFIX__-__PRIVATE_NAME__-spring-boot-starter

底层依赖私有 artifact `__PRIVATE_GROUP__:__PRIVATE_ARTIFACT__`。

> **frontmatter 的 `summary` 必须填。** 它会被 `tools/docs-index.sh` 抓进根 `AGENTS.md`
> 的模块索引；留着 TODO 的话文档卡口会红，CI 也会红。

## 怎么引入

```xml
<dependency>
    <groupId>__GROUP_ID__</groupId>
    <artifactId>__ARTIFACT_PREFIX__-__PRIVATE_NAME__-spring-boot-starter</artifactId>
</dependency>
```

TODO 补充最小可用配置（引入后还需要配什么才能跑起来）。

## 配置项

| key | 默认值 | 说明 |
|---|---|---|
| `__ARTIFACT_PREFIX__.__PRIVATE_NAME__.enabled` | `true` | 整体开关 |

TODO 补充其余配置项。

## 什么时候不该用它

TODO。

这一节最容易被省，也最值钱——没有它，组件会被用在不该用的地方，然后被骂。
想不出来时问自己：什么情况下用了它反而更糟？依赖太重？场景不匹配？有更简单的替代？

## ⚠️ 私有依赖的后果

`__PRIVATE_ARTIFACT__` 只存在于你的私服上。**在拉不到该 artifact 的机器上本模块会构建失败**——
CI、新同事的机器、以及将来若要开源。

若需要让骨架在任意机器上可构建，两条路：把该 artifact 发到公开仓库，或把本模块从根 pom 的
`<modules>` 里摘掉。
