package __BASE_PACKAGE__.component.web.doc;

import __BASE_PACKAGE__.component.web.config.__CLASS_PREFIX__WebProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;

/**
 * springdoc 配置。加一个 Bearer token 输入框，否则文档页上所有需要鉴权的接口都没法试。
 *
 * <p>生产环境用 {@code __ARTIFACT_PREFIX__.web.doc.enabled=false} 关闭。
 */
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI __ARTIFACT_PREFIX__OpenAPI(__CLASS_PREFIX__WebProperties properties) {
        return new OpenAPI()
                .info(new Info()
                        .title(properties.getDoc().getTitle())
                        .version(properties.getDoc().getVersion()))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME));
    }
}
