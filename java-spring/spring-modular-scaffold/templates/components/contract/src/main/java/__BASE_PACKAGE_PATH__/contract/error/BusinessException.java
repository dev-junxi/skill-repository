package __BASE_PACKAGE__.contract.error;

/**
 * 业务异常。由 web 组件的全局异常处理器统一翻译成 {@link __BASE_PACKAGE__.contract.response.R}。
 *
 * <p>注意：异常 message 会被日志记录，<b>不得把用户内容原文拼进去</b>——
 * 那会让加密存储的边界在日志里全部失效。只记标识与位置信息。
 */
public class BusinessException extends RuntimeException {

    private final transient ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
