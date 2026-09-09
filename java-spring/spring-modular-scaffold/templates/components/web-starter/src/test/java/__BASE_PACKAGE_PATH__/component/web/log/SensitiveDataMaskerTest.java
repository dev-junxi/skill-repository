package __BASE_PACKAGE__.component.web.log;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class SensitiveDataMaskerTest {

    private final SensitiveDataMasker masker =
            new SensitiveDataMasker(List.of("password", "token", "secret", "authorization", "api_key"), 100);

    @Test
    void masks_sensitive_top_level_field() {
        String json = "{\"username\":\"alice\",\"password\":\"hunter2\"}";
        assertThat(masker.mask(json)).contains("\"username\":\"alice\"").contains("\"password\":\"***\"");
    }

    @Test
    void masks_nested_sensitive_field() {
        String json = "{\"user\":{\"name\":\"bob\",\"api_key\":\"sk-abc123\"}}";
        String out = masker.mask(json);
        assertThat(out).contains("\"name\":\"bob\"");
        assertThat(out).doesNotContain("sk-abc123");
    }

    @Test
    void masks_field_inside_array() {
        String json = "{\"items\":[{\"token\":\"t1\"},{\"token\":\"t2\"}]}";
        String out = masker.mask(json);
        assertThat(out).doesNotContain("t1").doesNotContain("t2");
    }

    /** 字段名大小写不敏感——Authorization 头常见写法是首字母大写 */
    @Test
    void masking_is_case_insensitive() {
        String json = "{\"Authorization\":\"Bearer xyz\"}";
        assertThat(masker.mask(json)).doesNotContain("xyz");
    }

    @Test
    void truncates_body_over_limit() {
        String json = "{\"note\":\"" + "x".repeat(500) + "\"}";
        String out = masker.mask(json);
        assertThat(out).hasSizeLessThanOrEqualTo(100).endsWith("...<truncated>");
    }

    /** 非 JSON 内容不能因为解析失败就原样落盘，长度限制照样生效 */
    @Test
    void non_json_is_truncated_not_parsed() {
        String out = masker.mask("x".repeat(500));
        assertThat(out).endsWith("...<truncated>");
    }

    @Test
    void empty_body_returns_empty() {
        assertThat(masker.mask("")).isEmpty();
        assertThat(masker.mask(null)).isEmpty();
    }

    /** 路径前缀命中时整体不记录——响应体全是用户自然语言的端点走这条 */
    @Test
    void redacted_path_prefix_suppresses_whole_body() {
        SensitiveDataMasker m =
                new SensitiveDataMasker(List.of("password"), 4096, List.of("/v1/care/inbox", "/v1/care/messages"));
        assertThat(m.shouldRedactPath("/v1/care/inbox/list")).isTrue();
        assertThat(m.shouldRedactPath("/v1/care/messages")).isTrue();
        assertThat(m.shouldRedactPath("/v1/user/profile")).isFalse();
        assertThat(m.shouldRedactPath(null)).isFalse();
    }

    /** 默认构造不带路径前缀时，任何路径都不该被整体抑制 */
    @Test
    void no_redact_paths_configured_means_nothing_suppressed() {
        assertThat(masker.shouldRedactPath("/v1/care/inbox")).isFalse();
    }
}
