---
module: __ARTIFACT_PREFIX__-auth-spring-boot-starter
type: component
summary: token 验签与 RequestUser 注入（Sa-Token JWT Stateless），无状态不碰 DB
---

# __ARTIFACT_PREFIX__-auth-spring-boot-starter

## 怎么引入

```xml
<dependency>
    <groupId>__GROUP_ID__</groupId>
    <artifactId>__ARTIFACT_PREFIX__-auth-spring-boot-starter</artifactId>
</dependency>
```

Controller 里：

```java
@GetMapping("/v1/me")
public R<Profile> me(@CurrentUser RequestUser user) { ... }
```

## 配置项

| key | 默认值 | 说明 |
|---|---|---|
| `__ARTIFACT_PREFIX__.auth.enabled` | `true` | 整体开关 |
| `__ARTIFACT_PREFIX__.auth.access-token-ttl` | `2h` | 短 TTL 是 Stateless 下撤销能力的替代品 |
| `__ARTIFACT_PREFIX__.auth.refresh-token-ttl` | `90d` | refresh token 只含 userId |
| `sa-token.jwt-secret-key` | — | **必须走环境变量** `__ENV_PREFIX___JWT_SECRET`，绝不提交进 yml |

## 边界

本组件**只做验签与解析**，不签发 token、不碰 DB。签发在 `server/__ARTIFACT_PREFIX__-auth` 业务模块。

分开的理由：未来拆服务时**每个服务都需要"解析 token"，只有 auth 服务需要"签发"**。

**Sa-Token 不得出现在本包与 `__BASE_PACKAGE__.server.auth` 之外**（ArchUnit R11 强制）。业务模块一律只认 `@CurrentUser RequestUser`——这样 Sa-Token 是可替换的实现细节，而不是渗透全库的框架。

## Stateless 模式的代价

用的是 `StpLogicJwtForStateless`（ADR-0009）。这不是偏好——ADR-0007 要求服务端不为鉴权查库，只有 Stateless 满足；它同时让多实例部署不需要 Redis。

代价必须知道：

- **没有强制下线 / 踢人能力**，token 签发即生效到过期
- 没有 `SaSession`，不能查在线用户列表
- 注销靠客户端删 token + 2h TTL 自然过期

**别把"踢人下线""多端登录互踢"这些 Sa-Token 招牌能力算进选型收益**——Stateless 下都不可用。

## isPro 的 fail-closed 约定

claim 缺失、或值无法识别（如 `"yes"` / `1`）时一律按 `false`。只有明确的 `true` / `"true"` 才算 Pro。把免费用户误判成 Pro 是直接收入损失，反过来只是多一次升级提示。
