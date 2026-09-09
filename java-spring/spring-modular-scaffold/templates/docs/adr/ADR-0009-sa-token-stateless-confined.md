# ADR-0009: Sa-Token 用 JWT Stateless 模式，且严格隔离在 auth 内

Status: Accepted (2026-09-08)

Context:
  Sa-Token 有四种工作模式，服务端存不存 token 取决于选哪种：

  | 模式 | token | 服务端存储 | 多实例 |
  |---|---|---|---|
  | 默认（Session） | 随机串 | SaTokenDao，默认单机内存 Map | 必须换 Redis |
  | JWT Simple / Mixin | JWT | 仍依赖 SaTokenDao | 同上 |
  | **JWT Stateless** | JWT 自包含 | **零存储** | **天然支持** |

Decision:
  用 `StpLogicJwtForStateless`。这不是偏好——ADR-0007 要求服务端不为鉴权查库，
  只有 Stateless 满足；它同时让 ADR-0011 的多实例部署不需要 Redis。

  **Sa-Token 只允许出现在 `__BASE_PACKAGE__.component.auth..` 与 `__BASE_PACKAGE__.server.auth..`。**
  业务模块一律只认 `@CurrentUser RequestUser`。

Consequences:
  - **没有强制下线 / 踢人能力**，token 签发即生效到过期。由 2 小时 access TTL
    作为撤销能力的替代品
  - 没有 SaSession，不能查在线用户列表
  - ⚠️ **别把"踢人下线""多端登录互踢"这些 Sa-Token 招牌能力算进选型收益**——
    Stateless 下都不可用。选它的实际理由是权限注解与角色权限模型
  - 隔离的意义：Sa-Token 因此是可替换的实现细节。放任 StpUtil 蔓延到 domain
    会让 ADR-0003 的纯净性要求形同虚设

Verify:
  mvn -pl tools/__ARTIFACT_PREFIX__-archunit test   # R11
  grep -rln "cn.dev33.satoken" server/ components/   # 只应出现在 auth 相关模块
