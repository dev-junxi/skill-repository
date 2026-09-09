package __BASE_PACKAGE__.component.auth;

/**
 * access token 的 claim 键名。
 *
 * <p>这些是签发方（server/__ARTIFACT_PREFIX__-auth）与解析方（本组件）之间的契约，改动必须两边同步。
 */
public final class TokenClaims {

    public static final String USER_ID = "uid";
    public static final String CUSTOM_USER_ID = "cuid";
    public static final String IS_PRO = "pro";

    private TokenClaims() {}
}
