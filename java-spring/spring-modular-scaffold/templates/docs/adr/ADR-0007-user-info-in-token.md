# ADR-0007: 用户信息进 token，短 access + 长 refresh

Status: Accepted (2026-09-08)

> ⚠️ **本条的 claim 内容必须按你的业务重写。**
> 模板里列的字段（用户标识、订阅状态 `isPro`）来自原型项目的业务模型。
> 保留的是「用户信息进 token、服务端不为鉴权查库、短 access + 长 refresh」
> 这套**结构**；具体放哪些 claim 是你的业务决策——照抄就是假的判例，
> 而 ADR 的全部价值在于它记录的是真实做过的决定。

Context:
  参考实现的 require_auth 每次请求都查库拿 is_pro，是明确的性能负担。
  但把 is_pro 放进 token 有个更大的坑：参考实现的 JWT 有效期 **365 天**，
  用户买了 Pro 之后 token 里仍是 isPro=false，**最长一年都是免费用户**。
  这是产品级 bug，不是技术偏好。

Decision:
  服务端不为鉴权查库，RequestUser 全部字段来自 access token 的 claim。
  配套两种 token：

  | | TTL | claim |
  |---|---|---|
  | access | 2 小时 | userId、customUserId、isPro |
  | refresh | 90 天 | 只有 userId，**不含任何权限信息** |

Consequences:
  - 订阅变更后：客户端主动刷新 token → 立即生效；未刷新时最长 2 小时自然生效
  - MUST isPro 缺失或值无法识别时一律按 false（fail-closed）。把免费用户误判成
    Pro 是直接收入损失，反过来只是多一次升级提示
  - 需要 iOS 端配合改造成双 token
  - 无服务端强制下线能力，见 ADR-0009

Verify:
  RequestUserAssemblerTest.missing_is_pro_defaults_to_false
  RequestUserAssemblerTest.unrecognised_is_pro_value_defaults_to_false
