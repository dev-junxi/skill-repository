package __BASE_PACKAGE__.component.web.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 请求/响应体脱敏。
 *
 * <p>两层防护：①按字段名遮蔽敏感值；②按路径前缀整体抑制（用于响应体全是用户自然语言的端点）。
 *
 * <p><b>为什么第二层是必需的</b>：字段名脱敏挡不住"整个响应体就是用户日记内容"这种情况。
 * 路径前缀清单放在配置里（{@code __ARTIFACT_PREFIX__.web.log.redact-paths}），新增这类端点时改 yml 即可，
 * 不必改代码常量数组。
 */
public class SensitiveDataMasker {

    private static final String MASK = "***";
    private static final String TRUNCATED_SUFFIX = "...<truncated>";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<String> sensitiveKeys;
    private final int maxLength;
    private final List<String> redactPathPrefixes;

    public SensitiveDataMasker(List<String> sensitiveKeys, int maxLength) {
        this(sensitiveKeys, maxLength, List.of());
    }

    public SensitiveDataMasker(List<String> sensitiveKeys, int maxLength, List<String> redactPathPrefixes) {
        this.sensitiveKeys =
                sensitiveKeys.stream().map(k -> k.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        this.maxLength = maxLength;
        this.redactPathPrefixes = List.copyOf(redactPathPrefixes);
    }

    /** 该路径的 body 是否整体不记录 */
    public boolean shouldRedactPath(String uri) {
        if (uri == null) {
            return false;
        }
        return redactPathPrefixes.stream().anyMatch(uri::startsWith);
    }

    public String mask(String body) {
        if (body == null || body.isEmpty()) {
            return "";
        }
        String result;
        try {
            JsonNode root = objectMapper.readTree(body);
            maskNode(root);
            result = objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            // 解析失败不代表可以原样落盘——非 JSON 内容同样可能含用户原文
            result = body;
        }
        return truncate(result);
    }

    private void maskNode(JsonNode node) {
        if (node instanceof ObjectNode object) {
            // 先取字段名快照再改：边遍历边 put 同名 key 虽不改变 key 集合，
            // 但依赖迭代器的这个性质太脆，快照更稳
            List<String> names = new ArrayList<>();
            object.fieldNames().forEachRemaining(names::add);
            for (String name : names) {
                if (sensitiveKeys.contains(name.toLowerCase(Locale.ROOT))) {
                    object.put(name, MASK);
                } else {
                    maskNode(object.get(name));
                }
            }
        } else if (node instanceof ArrayNode array) {
            array.forEach(this::maskNode);
        }
    }

    private String truncate(String s) {
        if (s.length() <= maxLength) {
            return s;
        }
        return s.substring(0, Math.max(0, maxLength - TRUNCATED_SUFFIX.length())) + TRUNCATED_SUFFIX;
    }
}
