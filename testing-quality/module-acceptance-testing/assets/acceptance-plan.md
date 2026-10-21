---
title: 模块验收计划
summary: 模块边界、分级业务用例、断言证据与隔离清理设计
---

# {{module_name}} 验收计划

此文件是待绑定模板；在目标项目中填写双花括号参数，并删除不适用的段落。

## 目标与执行范围

- 被测能力：{{capabilities}}
- 公共入口与实际版本：{{public_boundary_and_version}}
- 本轮模式：{{design_or_offline_or_isolated_or_live}}
- 已授权的环境、写入范围和真实费用：{{authorized_scope}}
- 外部副作用如何隔离：{{sandbox_or_substitute}}
- 完整用例目录、选中计划与发布必需项：{{catalog_selection_and_required_cases}}

## 影响范围

| 文件/入口 | 职责与修改 | 复用依赖 | 验证方法 |
|---|---|---|---|
| {{file}} | {{change}} | {{existing_dependency}} | {{observable_check}} |

## 用例矩阵

| 用例ID | 优先级 | 业务场景与前置 | 合成输入/操作序列 | 预期与禁止结果 | 可观测证据 | 资源与清理 |
|---|---|---|---|---|---|---|
| {{stable_case_id}} | {{priority}} | {{scenario_and_preconditions}} | {{input_and_actions}} | {{assertions}} | {{read_endpoint_or_evidence}} | {{owned_resources_and_cleanup}} |

权限、过滤、删除等反例的正命中前置：{{positive_control}}。
开放语义判据/独立评审或REVIEW条件：{{semantic_evaluation}}。
时间、身份、版本和分页边界：{{boundary_conditions}}。

## 生命周期与故障处理

- run_id与资源归属标记：{{ownership}}
- 创建回执丢失后的定位方式：{{lost_ack_recovery}}
- 删除后确认入口及最长等待：{{deletion_verification}}
- 恢复清理命令、环境校验与有限重试：{{cleanup_recovery}}
- 异步终态、超时与可安全重试的操作：{{async_policy}}
- 预期业务错误与环境不可用的区分：{{error_classification}}
- 状态/退出码/部分执行规则：{{result_policy}}

## 验证与报告

| 验证层 | 命令/入口 | 能证明什么 | 不能证明什么 |
|---|---|---|---|
| 离线契约与执行器 | {{offline_command}} | {{offline_evidence}} | 真实服务语义与外部副作用 |
| 隔离集成或沙箱 | {{isolated_command}} | {{isolated_evidence}} | 未覆盖的真实部署行为 |
| 真实验收（若已授权） | {{live_command}} | {{live_evidence}} | 未选中/未执行范围 |

报告目录：{{run_output_directory}}；文档/格式/架构卡口：{{required_checks}}。
指标及分母：{{metrics_and_denominators}}；未采集项：{{unmeasured_items}}。

## 执行后补充

- 运行ID、版本来源与选中范围：{{actual_run_metadata}}
- 状态分布、具体失败和证据：{{actual_results}}
- 清理结果/残留：{{cleanup_outcome}}
- 本轮限制与下一步：{{remaining_gaps}}
