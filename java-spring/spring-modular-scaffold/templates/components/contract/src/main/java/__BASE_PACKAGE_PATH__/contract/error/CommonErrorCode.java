package __BASE_PACKAGE__.contract.error;

/** 跨模块通用的错误码。模块专属错误码定义在各自的 -api module 里。 */
public enum CommonErrorCode implements ErrorCode {
    SUCCESS(0, "成功"),
    BAD_REQUEST(400, "请求参数不合法"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权访问"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    CommonErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
