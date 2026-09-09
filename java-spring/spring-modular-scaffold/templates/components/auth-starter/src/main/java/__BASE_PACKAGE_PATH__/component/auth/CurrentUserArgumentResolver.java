package __BASE_PACKAGE__.component.auth;

import cn.dev33.satoken.stp.StpUtil;
import __BASE_PACKAGE__.contract.error.BusinessException;
import __BASE_PACKAGE__.contract.error.CommonErrorCode;
import __BASE_PACKAGE__.contract.user.RequestUser;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** 把 Sa-Token 的 claim 翻译成 {@link RequestUser}，业务侧因此完全不认识 Sa-Token。 */
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final RequestUserAssembler assembler;

    public CurrentUserArgumentResolver(RequestUserAssembler assembler) {
        this.assembler = assembler;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && RequestUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
        boolean required = annotation == null || annotation.required();

        if (!StpUtil.isLogin()) {
            if (required) {
                throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
            }
            return null;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(TokenClaims.USER_ID, StpUtil.getExtra(TokenClaims.USER_ID));
        claims.put(TokenClaims.CUSTOM_USER_ID, StpUtil.getExtra(TokenClaims.CUSTOM_USER_ID));
        claims.put(TokenClaims.IS_PRO, StpUtil.getExtra(TokenClaims.IS_PRO));
        return assembler.assemble(claims);
    }
}
