# ADR-0012: 定时任务分两类，不自建 job 组件

Status: Accepted (2026-09-08)

Context:
  多实例下 @Scheduled 会在每个实例都跑。参考项目有一个基于 xxl-job 的 job starter，
  但 xxl-job 需要独立部署调度中心，对当前这个单体是拿大炮打蚊子。

Decision:
  不自建 job 组件。按"多个实例同时跑会不会出问题"分两类：

  | 类型 | 判据 | 方案 |
  |---|---|---|
  | 可并行认领 | 工作项独立，谁抢到谁处理 | **DB 批量认领**：UPDATE ... SET claimed_by=?, claimed_at=? WHERE state=? AND next_action_at<=? LIMIT n，再 SELECT 认领到的 |
  | 全局唯一 | 全量扫描 / 聚合 / 报表，跑两次 = 重复产出 | **ShedLock**（DB 表实现，不引 Redis）+ @Scheduled |

Consequences:
  - 第一类天然多实例安全，是参考实现已验证的设计，直接继承
  - worker 需要"按条目隔离错误 + 失败次数上限 + 到达上限能落终态"三件套，
    且**终态在状态机里必须对早期状态可达**——参考实现踩过：Failed 只有投递期
    状态能进，而真正会卡死的恰恰是早期状态。这是业务约束不是组件能力，
    写进各模块自己的 AGENTS.md

Verify:
  出现第一个全局唯一任务时，检查是否用了 ShedLock 而非裸 @Scheduled
