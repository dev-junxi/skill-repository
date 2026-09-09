package __BASE_PACKAGE__.component.auth;

import cn.dev33.satoken.jwt.StpLogicJwtForStateless;
import cn.dev33.satoken.stp.StpLogic;
import __BASE_PACKAGE__.component.auth.config.__CLASS_PREFIX__AuthProperties;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 的 <b>JWT Stateless 无状态模式</b>（ADR-0009）。
 *
 * <p>这不是偏好——服务端不为鉴权查库（ADR-0007）要求所有信息自包含在 token 里，
 * 只有 Stateless 满足；它同时让多实例部署不需要任何共享存储（ADR-0011）。
 *
 * <p><b>代价</b>：没有强制下线/踢人能力，没有 SaSession，注销靠客户端删 token +
 * 2 小时 TTL 自然过期。要做强制下线需引入 token 黑名单存储，届时另开 ADR。
 */
@AutoConfiguration
@EnableConfigurationProperties(__CLASS_PREFIX__AuthProperties.class)
@ConditionalOnProperty(prefix = "__ARTIFACT_PREFIX__.auth", name = "enabled", havingValue = "true", matchIfMissing = true)
public class __CLASS_PREFIX__AuthAutoConfiguration implements WebMvcConfigurer {

    private final RequestUserAssembler assembler = new RequestUserAssembler();

    @Bean
    @ConditionalOnMissingBean(StpLogic.class)
    public StpLogic stpLogicJwtForStateless() {
        return new StpLogicJwtForStateless();
    }

    @Bean
    public RequestUserAssembler requestUserAssembler() {
        return assembler;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver(assembler));
    }
}
