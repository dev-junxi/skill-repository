package __BASE_PACKAGE__.component.auth;

import __BASE_PACKAGE__.contract.error.BusinessException;
import __BASE_PACKAGE__.contract.error.CommonErrorCode;
import __BASE_PACKAGE__.contract.user.RequestUser;
import java.util.Map;

/**
 * claim → RequestUser。纯函数，与 Sa-Token 无耦合，便于单测。
 *
 * <p><b>isPro 缺失或无法识别时一律按 false</b>——fail-closed。把免费用户误判成 Pro 是直接的
 * 收入损失，反过来只是用户多看到一次升级提示。
 */
public class RequestUserAssembler {

    public RequestUser assemble(Map<String, Object> claims) {
        Long id = toLong(claims.get(TokenClaims.USER_ID));
        if (id == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED, "token 缺少 userId");
        }
        Object customId = claims.get(TokenClaims.CUSTOM_USER_ID);
        return new RequestUser(
                id, customId == null ? null : customId.toString(), toBoolean(claims.get(TokenClaims.IS_PRO)));
    }

    private Long toLong(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(raw.toString());
        } catch (NumberFormatException e) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED, "token 的 userId 格式非法");
        }
    }

    /** 只有明确的 true / "true" 才算 Pro，其余一律 false */
    private boolean toBoolean(Object raw) {
        if (raw instanceof Boolean b) {
            return b;
        }
        return raw != null && "true".equalsIgnoreCase(raw.toString());
    }
}
