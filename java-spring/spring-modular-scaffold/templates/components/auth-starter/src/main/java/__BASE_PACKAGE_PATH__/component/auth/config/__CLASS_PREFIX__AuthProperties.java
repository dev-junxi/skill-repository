package __BASE_PACKAGE__.component.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "__ARTIFACT_PREFIX__.auth")
public class __CLASS_PREFIX__AuthProperties {

    private boolean enabled = true;

    /** access token 有效期。短 TTL 是 Stateless 模式下"撤销能力"的替代品（ADR-0007） */
    private Duration accessTokenTtl = Duration.ofHours(2);

    /** refresh token 有效期，只含 userId，不含任何权限信息 */
    private Duration refreshTokenTtl = Duration.ofDays(90);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    public Duration getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setRefreshTokenTtl(Duration refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }
}
