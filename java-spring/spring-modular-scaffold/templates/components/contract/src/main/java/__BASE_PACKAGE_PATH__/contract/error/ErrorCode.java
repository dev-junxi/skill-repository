package __BASE_PACKAGE__.contract.error;

/** 错误码契约。各业务模块定义自己的枚举实现本接口，避免所有错误码挤在一个巨型枚举里。 */
public interface ErrorCode {

    /** 业务错误码，0 表示成功 */
    int code();

    /** 面向用户的错误描述 */
    String message();
}
