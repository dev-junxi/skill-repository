package __BASE_PACKAGE__.component.web.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "__ARTIFACT_PREFIX__.web")
public class __CLASS_PREFIX__WebProperties {

    private boolean enabled = true;
    private final Log log = new Log();
    private final Doc doc = new Doc();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Log getLog() {
        return log;
    }

    public Doc getDoc() {
        return doc;
    }

    public static class Log {
        /** JSON 里需要遮蔽值的字段名，大小写不敏感 */
        private List<String> sensitiveKeys =
                List.of("password", "token", "secret", "authorization", "api_key", "apiKey", "accessToken");

        /** body 记录上限，超出截断 */
        private int maxBodyLength = 4096;

        /**
         * 响应体整体不记录的路径前缀，用于响应体全是用户自然语言的端点。
         * 新增这类端点时必须把前缀加进来——放配置而非代码常量，是为了让它和端点定义
         * 在同一个心智半径里。
         */
        private List<String> redactPaths = List.of();

        public List<String> getSensitiveKeys() {
            return sensitiveKeys;
        }

        public void setSensitiveKeys(List<String> sensitiveKeys) {
            this.sensitiveKeys = sensitiveKeys;
        }

        public int getMaxBodyLength() {
            return maxBodyLength;
        }

        public void setMaxBodyLength(int maxBodyLength) {
            this.maxBodyLength = maxBodyLength;
        }

        public List<String> getRedactPaths() {
            return redactPaths;
        }

        public void setRedactPaths(List<String> redactPaths) {
            this.redactPaths = redactPaths;
        }
    }

    public static class Doc {
        private String title = "__CLASS_PREFIX__ API";
        private String version = "v1";
        /** 生产环境应设为 false */
        private boolean enabled = true;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
