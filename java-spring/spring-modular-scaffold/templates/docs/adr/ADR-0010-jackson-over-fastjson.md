# ADR-0010: JSON 统一 Jackson，禁用 fastjson2

Status: Accepted (2026-09-08)

Context:
  参考的 parent 模板里带着 fastjson2。

Decision:
  统一用 Jackson 2.x（版本由 Spring Boot BOM 管理），不引入 fastjson2。

  三条理由：
  1. **安全**：fastjson 1.x 有连续多年的 autoType 反序列化 RCE 史，fastjson2
     重写后改善但仍有 CVE 记录。它的卖点是"快"——而本项目瓶颈是 AI 上游响应
     （秒级），JSON 解析（微秒级）快不快完全无关。**用安全风险换一个测不出来的
     性能收益**
  2. **框架原生**：Spring Boot 自动配置的就是 Jackson。换 fastjson2 要替换
     HttpMessageConverter，然后 springdoc 的 schema 生成、@JsonProperty vs
     @JSONField 全都要额外适配
  3. **一致性**：混用会出现"Controller 出参走 Jackson 规则、内部缓存走 fastjson
     规则"，日期格式 / null 处理 / 命名策略不一致，这类 bug 极难查

Consequences:
  - contract 只允许依赖 jackson-annotations，不引 jackson-databind
    （那会拖进整个序列化实现，破坏 ADR-0013 的零 Spring 边界意图）

Verify:
  grep -rn "fastjson" __ARTIFACT_PREFIX__-parent/pom.xml components/ server/   # 应无匹配
