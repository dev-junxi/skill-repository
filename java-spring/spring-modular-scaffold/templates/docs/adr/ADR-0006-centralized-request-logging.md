# ADR-0006: 请求日志统一中间件，业务层禁止重复打印

Status: Accepted (2026-09-08)

Context:
  请求/响应日志散落在各 handler 里会导致：格式不一、traceId 缺失、
  以及最危险的——**绕过脱敏**。

Decision:
  `__ARTIFACT_PREFIX__-web-spring-boot-starter` 统一记录 method / uri / status / 耗时 /
  请求体 / 响应体，按状态码分级（2xx-3xx→info，4xx→warn，5xx→error）。
  traceId 由 TraceIdFilter 放进 MDC，logback pattern 里 %X{traceId} 自动注入。

Consequences:
  - MUST NOT 业务代码再打印请求体/响应体，也不得手动生成 traceId
  - 脱敏两层：字段名遮蔽（password/token/secret/...）+ 路径前缀整体抑制
  - MUST 新增"响应体是用户自然语言"的端点时，把路径前缀加进
    `__ARTIFACT_PREFIX__.web.log.redact-paths`。**这一项放配置而非代码常量数组**——参考实现
    把它硬编码在代码里，导致"新增端点必须记得改那个数组"，纯靠自觉
  - 可插拔：`__ARTIFACT_PREFIX__.web.enabled=false` 整体关闭，业务模块零改动

Verify:
  SensitiveDataMaskerTest（9 个用例覆盖嵌套/数组/大小写/截断/非JSON/路径前缀）
