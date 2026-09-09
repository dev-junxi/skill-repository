package __BASE_PACKAGE__.component.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入当前请求的 {@link __BASE_PACKAGE__.contract.user.RequestUser}。
 *
 * <p>业务模块的 Controller <b>只能</b>用这个注解获取当前用户，不得直接调 {@code StpUtil}
 * 或依赖 auth 模块的任何类型（ArchUnit R11 强制）。
 *
 * <pre>{@code
 * @GetMapping("/v1/me")
 * public R<Profile> me(@CurrentUser RequestUser user) { ... }
 * }</pre>
 *
 * @see #required() 为 false 时未登录返回 null，用于可选认证的端点
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
    boolean required() default true;
}
