# spring-modular-scaffold

生成架构边界由构建期卡口强制的 Spring Boot 骨架。用法见 `SKILL.md`。

## 验收记录

| 组合 | 日期 | verify.sh | 基线 |
|---|---|---|---|
| 全关（无鉴权/无DB/monorepo） | 2026-09-09 | 通过 5 · 失败 0 · 跳过 1 | 5 pom · 25 java · 7 ADR |

| 全选（鉴权+DB+独立parent） | 2026-09-09 | 通过 6 · 失败 0 · 跳过 0 | 6 pom · 62 java · 11 ADR |
| 全选 + 私有组件接线 | 2026-09-09 | 通过 6 · 失败 0 · 跳过 0 | 复验，见下注 |

全关组合的跳过项为 check-migrations——该组合无持久层，属预期。

全选组合含 Testcontainers 真实起 MySQL。首次接完私有组件后的那一轮
`mvn test` 曾偶发失败一次，直接重跑与再次 verify 均通过，判定为
Testcontainers 容器启动抖动而非生成器缺陷——如实记录，未粉饰为一次通过。
