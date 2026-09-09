package __BASE_PACKAGE__.contract.user;

/**
 * 请求上下文里的用户身份，由 auth 组件从 token claim 解析后注入。
 *
 * <p>业务模块的 Controller 一律通过 {@code @CurrentUser RequestUser} 获取当前用户，
 * 不得直接依赖 auth 模块的任何类型（架构铁律，ArchUnit R11 强制）。
 *
 * <p>字段全部来自 access token 的 claim，服务端不为鉴权查库（ADR-0007）。
 */
public record RequestUser(Long id, String customUserId, boolean isPro) {}
