package __BASE_PACKAGE__.component.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import __BASE_PACKAGE__.contract.error.BusinessException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RequestUserAssemblerTest {

    private final RequestUserAssembler assembler = new RequestUserAssembler();

    private Map<String, Object> claims(Object id, Object customId, Object isPro) {
        Map<String, Object> m = new HashMap<>();
        m.put(TokenClaims.USER_ID, id);
        m.put(TokenClaims.CUSTOM_USER_ID, customId);
        m.put(TokenClaims.IS_PRO, isPro);
        return m;
    }

    @Test
    void assembles_request_user_from_claims() {
        var user = assembler.assemble(claims(42L, "cus_abc", true));
        assertThat(user.id()).isEqualTo(42L);
        assertThat(user.customUserId()).isEqualTo("cus_abc");
        assertThat(user.isPro()).isTrue();
    }

    /** JWT 反序列化后数字常常是 Integer 而非 Long，不能直接强转 */
    @Test
    void tolerates_integer_user_id_from_jwt() {
        assertThat(assembler.assemble(claims(42, "cus_abc", false)).id()).isEqualTo(42L);
    }

    @Test
    void tolerates_string_boolean_for_is_pro() {
        assertThat(assembler.assemble(claims(1L, "c", "true")).isPro()).isTrue();
        assertThat(assembler.assemble(claims(1L, "c", "false")).isPro()).isFalse();
    }

    /** isPro 缺失时必须按 false 处理——fail-closed，绝不能把免费用户当成 Pro */
    @Test
    void missing_is_pro_defaults_to_false() {
        Map<String, Object> m = new HashMap<>();
        m.put(TokenClaims.USER_ID, 1L);
        m.put(TokenClaims.CUSTOM_USER_ID, "c");
        assertThat(assembler.assemble(m).isPro()).isFalse();
    }

    /** isPro 是无法识别的值时同样 fail-closed */
    @Test
    void unrecognised_is_pro_value_defaults_to_false() {
        assertThat(assembler.assemble(claims(1L, "c", "yes")).isPro()).isFalse();
        assertThat(assembler.assemble(claims(1L, "c", 1)).isPro()).isFalse();
    }

    @Test
    void missing_user_id_is_rejected() {
        Map<String, Object> m = new HashMap<>();
        m.put(TokenClaims.CUSTOM_USER_ID, "c");
        assertThatThrownBy(() -> assembler.assemble(m))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("userId");
    }

    @Test
    void malformed_user_id_is_rejected() {
        assertThatThrownBy(() -> assembler.assemble(claims("not-a-number", "c", true)))
                .isInstanceOf(BusinessException.class);
    }
}
