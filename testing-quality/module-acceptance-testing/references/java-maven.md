---
title: Java Maven 模块验收落地
summary: 将验收流程适配Java模块、现有客户端、JUnit和Maven，并提供一键入口与离线验证
---

# Java/Maven 适配

先读取目标仓库的规范，沿用已有依赖及包布局。参考是职责拆分示例，不要求每个模块复制同样数量的类。

## 目录与职责映射

```text
src/test/java/<模块包>/testcase/
  AcceptanceCase.java
  P0Samples.java / P1Samples.java / P2Samples.java
  AcceptanceContext.java
  AcceptanceRunner.java
  AcceptanceConfig.java
  AcceptanceEvidence.java
  AcceptanceResources.java
  AcceptanceReports.java
  ModuleAcceptanceIT.java
  ...Test.java
  run.sh
  README.md
  IMPLEMENTATION.md
```

如果项目把脚本或文档统一放到其他位置，跟随项目约定。生成文件输出到模块 `target/<module>-acceptance/<run_id>/`，不覆盖源码目录下的历史文档。

验收动作包括清理都用模块的稳定公共入口。客户端组件可以像参考一样用 `MemoryClient`；认证、订阅等Web模块应使用既有测试HTTP客户端和公开API，不能因此新增一套业务HTTP封装。底层DTO契约测试可以用MockWebServer，应用服务隔离集成测试按项目规范使用Testcontainers或既有fixtures。

不要把“不能绕过验收入口”错误推广到所有测试：仓储单元/集成测试仍可按仓库规则直测仓储；这里约束的是声称覆盖业务链路的验收案例。

## 分离离线验证和真实执行

用JUnit显式开关隔离真实验收，例如 `@EnabledIfSystemProperty(named="module.acceptance.live", matches="true")`；`<module>` 属性前缀应实际绑定模块名。普通 `mvn test` 不应因运行这套用例而访问真实服务。

先查看Surefire/Failsafe的include规则。有的项目把 `*IT` 纳入Surefire，有的只在 `verify` 阶段运行；类名和阶段不能照抄。证明开关关闭时真实服务未被访问、开关开启时选中用例确实执行，防止“零测试但BUILD SUCCESS”。

离线需要验证：

- 公共客户端路径编码、枚举值、snake_case、false、空对象/空列表和省略字段的差异。
- 列表与详情的不同响应形态、可空分页字段、非法JSON的安全异常。
- 样本实际发送的输入、报告状态分类、后续执行、资源归属与清理回执丢失。
- 报告JSON/Markdown中的计数、部分计划、无断言和空证据；不要只匹配标题文字。

Mockito回答不能当成真实服务业务行为的证明。用户要求LLM语义验证时，按项目规范使用真实提供方及独立评审，或显式REVIEW。

## 一键入口语义

按模块需要实现或复用以下模式，具体flags可以沿用现有CLI：

| 模式 | 语义 |
|---|---|
| `--verify` | 模块及必要依赖的离线测试，不访问真实服务 |
| `--run` / `--priorities P0,P1` | 显式真实验收；目标、范围和费用已在当前任务授权内 |
| `--cases P0-009` | 存在独立复测需求时按稳定ID筛选；未知/空匹配ID报错，不静默运行空计划 |
| `--cleanup /abs/path/resources.json` | 校验环境和资源归属后恢复清理，保留原结果 |

`--cases` 是可按需增加的模式，参考实现原本只按优先级筛选，不要声称直接复制后就支持。

脚本从自身目录定位仓库，校验JDK及CLI参数，使用shell数组传Maven参数，不用eval。目标URL不能带鉴权参数；凭证由环境变量/既有安全配置提供，不放到进程命令行或报告里。不要把参考端口8889、固定银行前缀或macOS JDK安装目录写入新模块默认配置。

例如读取用户指定的模块和JDK后，在目标仓库根目录运行（变量须先绑定到真实目录）：

```bash
export JAVA_HOME="$task_jdk_home"
mvn -pl "$task_module" -am -Dsurefire.failIfNoSpecifiedTests=false test
```

`failIfNoSpecifiedTests=false` 只在聚合构建需要时使用；始终检查目标模块实际运行了预期测试。真实开关、认证配置和构建阶段应在本模块README写明。不要执行未经授权的部署、扣款或发信来验证入口。

## 文档与收尾

复制skill附带的计划和说明模板时，绑定模块名、命令、指标、权限及清理策略，删除不适用段落。遵守front matter规则；参考Lumos要求普通Markdown含 `title` 和 `summary`，首行为 `---`，其他项目以自身规则为准。

格式化限定本次修改的文件，不顺手改掉工作区其他改动。适用时执行Spotless、模块与依赖测试、文档卡口。Maven命令按仓库规定选择JDK，不能假设执行机的默认版本正确。

Lumos参考命令（仅当当前目标确为该仓库且命令仍存在时）：

```bash
./tools/check-docs-frontmatter.sh
./tools/docs-index.sh --check
```

规则卡口报错时修正生成物，不改检查脚本来放行。前端、Python或其他栈沿用同样结果契约，替换实现工具，不引入Maven。
