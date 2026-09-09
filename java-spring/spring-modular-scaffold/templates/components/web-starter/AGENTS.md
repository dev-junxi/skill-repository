---
module: __ARTIFACT_PREFIX__-web-spring-boot-starter
type: component
summary: Web 层横切——traceId、请求日志与脱敏、全局异常、springdoc API 文档
---

# __ARTIFACT_PREFIX__-web-spring-boot-starter

## 怎么引入

```xml
<dependency>
    <groupId>__GROUP_ID__</groupId>
    <artifactId>__ARTIFACT_PREFIX__-web-spring-boot-starter</artifactId>
</dependency>
```

零配置可用。关闭：`__ARTIFACT_PREFIX__.web.enabled=false`。

## 配置项

| key | 默认值 | 说明 |
|---|---|---|
| `__ARTIFACT_PREFIX__.web.enabled` | `true` | 整体开关 |
| `__ARTIFACT_PREFIX__.web.log.sensitive-keys` | password/token/secret/authorization/api_key/apiKey/accessToken | JSON 里遮蔽值的字段名，大小写不敏感 |
| `__ARTIFACT_PREFIX__.web.log.max-body-length` | `4096` | body 记录上限，超出截断 |
| `__ARTIFACT_PREFIX__.web.log.redact-paths` | `[]` | **响应体整体不记录的路径前缀** |
| `__ARTIFACT_PREFIX__.web.doc.enabled` | `true` | API 文档开关，生产设 false |

## 铁律

**业务代码禁止再打印请求体/响应体。** 本组件已完整记录；重复打印既是噪音，也绕过了这里的脱敏（ADR-0006）。

**新增"响应体是用户自然语言"的端点时，必须把路径前缀加进 `redact-paths`。** 漏了就等于用户原文全量落盘。

## 已知行为

`ContentCachingRequestWrapper` 只缓存**应用实际读取过**的请求体字节。如果请求在读 body 之前就失败（例如 405 方法不允许），日志里 `req=` 会是空的——这不是脱敏失效，是根本没读到。

## 什么时候不该用它

- 非 Web 的模块（定时任务、CLI）不需要引入——它拖着 `spring-boot-starter-web`
- 需要发布领域事件不构成引入理由，事件基类在 `__ARTIFACT_PREFIX__-contract`
