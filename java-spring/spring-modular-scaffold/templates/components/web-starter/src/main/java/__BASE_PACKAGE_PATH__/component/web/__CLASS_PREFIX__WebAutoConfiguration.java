package __BASE_PACKAGE__.component.web;

import __BASE_PACKAGE__.component.web.config.__CLASS_PREFIX__WebProperties;
import __BASE_PACKAGE__.component.web.doc.OpenApiConfig;
import __BASE_PACKAGE__.component.web.error.GlobalExceptionHandler;
import __BASE_PACKAGE__.component.web.log.RequestLoggingFilter;
import __BASE_PACKAGE__.component.web.log.SensitiveDataMasker;
import __BASE_PACKAGE__.component.web.log.TraceIdFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/** 可插拔：{@code __ARTIFACT_PREFIX__.web.enabled=false} 即整体关闭，业务模块零改动、零编译错误。 */
@AutoConfiguration
@EnableConfigurationProperties(__CLASS_PREFIX__WebProperties.class)
@ConditionalOnProperty(prefix = "__ARTIFACT_PREFIX__.web", name = "enabled", havingValue = "true", matchIfMissing = true)
public class __CLASS_PREFIX__WebAutoConfiguration {

    @Bean
    public SensitiveDataMasker sensitiveDataMasker(__CLASS_PREFIX__WebProperties properties) {
        return new SensitiveDataMasker(
                properties.getLog().getSensitiveKeys(),
                properties.getLog().getMaxBodyLength(),
                properties.getLog().getRedactPaths());
    }

    @Bean
    public TraceIdFilter traceIdFilter() {
        return new TraceIdFilter();
    }

    @Bean
    public RequestLoggingFilter requestLoggingFilter(SensitiveDataMasker masker) {
        return new RequestLoggingFilter(masker);
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @AutoConfiguration
    @ConditionalOnProperty(prefix = "__ARTIFACT_PREFIX__.web.doc", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Import(OpenApiConfig.class)
    static class DocAutoConfiguration {}
}
