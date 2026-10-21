---
title: 模块验收运行说明
summary: 模块验收的执行命令、配置、报告、结果判定与恢复清理
---

# {{module_name}} 验收测试

此文件是待绑定模板。替换双花括号参数、写明入口实际支持的模式，并按目标仓库规则调整front matter；不要发布未填写模板。

## 入口与配置

所有验收及清理动作通过 {{public_boundary}}。普通测试不访问真实服务，真实验收需显式开启。

| 模式 | 实际命令 | 环境与副作用 |
|---|---|---|
| 离线验证 | {{offline_command}} | 不访问真实服务 |
| 选定范围验收 | {{acceptance_command}} | {{target_and_authorized_side_effects}} |
| 恢复清理 | {{cleanup_command}} | 只操作原清单中归属确认的测试资源 |

| 配置 | 默认值或必填 | 用途 |
|---|---|---|
| {{target_config}} | {{target_default}} | 测试服务/隔离环境标识 |
| {{credential_config}} | {{credential_requirement}} | 从安全配置读取，禁止写入命令行/证据 |
| {{timeout_config}} | {{bounded_timeout}} | 单次调用及异步完成上限 |
| {{selection_config}} | {{selected_case_default}} | 选择优先级/用例，不覆盖未选中范围 |
| {{strict_mode_config}} | {{strict_mode_default}} | 必需项SKIP/REVIEW是否使进程失败 |

实际费用或外部副作用说明：{{cost_and_side_effects}}。
运行命令的工作目录和工具链要求：{{working_directory_and_toolchain}}。

## 报告

目录：{{output_root}}/{{run_id}}/。

- 分类报告：六列“用例ID、场景与输入、预期结果、实际结果与证据、结果、耗时(ms)”。
- summary.md：完整/选中/实际执行范围、状态分布、业务指标、失败与待复核、清理状态。
- results.json：每例状态、实际断言和证据引用。
- evidence：脱敏合成调用、耗时、错误和清理证据。
- run.json：版本来源、运行参数与选中计划，不含凭证。
- resources.json及cleanup-result.json：若有外部测试资源，保存归属和恢复清理结果。

先保存每例结果，再刷新分类和总体报告；中断不覆盖旧运行报告。

## 判定和边界

PASS表示必需自动检查通过且无必需待评审项；FAIL表示实际违反预期；ERROR表示未完成验证的非预期错误；UNSUPPORTED表示明确缺少能力；SKIP表示未执行；REVIEW表示证据还需复核。

本模块的退出码与必需能力策略：{{exit_policy}}。
本模块的开放语义评审方式：{{semantic_review_policy}}。
本模块的指标口径与未覆盖范围：{{metric_scope_and_limits}}。

离线或沙箱通过不代表真实服务通过；只选P0不代表整个模块验收通过。未测、未采集、不支持和零事件分别记录。

## 清理与恢复

资源隔离和归属：{{resource_ownership}}。
正常、失败和异常清理：{{cleanup_lifecycle}}。
创建回执丢失/最终一致性的处理：{{uncertain_state_handling}}。
清理失败后的精确定位、命令及人工处理边界：{{cleanup_recovery_details}}。

强制杀进程或服务失效时不能保证立即删除；保留原运行清单，恢复后只清理原环境和确认归属的数据，另存恢复结果。
