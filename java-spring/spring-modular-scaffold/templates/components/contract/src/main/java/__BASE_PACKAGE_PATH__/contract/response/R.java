package __BASE_PACKAGE__.contract.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import __BASE_PACKAGE__.contract.error.CommonErrorCode;
import __BASE_PACKAGE__.contract.error.ErrorCode;

/**
 * 统一响应体。
 *
 * @param code 0 表示成功，非 0 为业务错误码
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record R<T>(int code, String message, T data) {

    public static <T> R<T> ok(T data) {
        return new R<>(CommonErrorCode.SUCCESS.code(), CommonErrorCode.SUCCESS.message(), data);
    }

    public static R<Void> ok() {
        return ok(null);
    }

    public static <T> R<T> fail(ErrorCode errorCode) {
        return new R<>(errorCode.code(), errorCode.message(), null);
    }

    public static <T> R<T> fail(ErrorCode errorCode, String message) {
        return new R<>(errorCode.code(), message, null);
    }

    public boolean success() {
        return code == CommonErrorCode.SUCCESS.code();
    }
}
