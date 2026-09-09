package __BASE_PACKAGE__.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import __BASE_PACKAGE__.contract.error.BusinessException;
import __BASE_PACKAGE__.contract.error.CommonErrorCode;
import __BASE_PACKAGE__.contract.event.DomainEvent;
import __BASE_PACKAGE__.contract.response.PageData;
import __BASE_PACKAGE__.contract.response.R;
import __BASE_PACKAGE__.contract.user.RequestUser;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ContractTest {

    @Test
    void ok_wraps_data_with_success_code() {
        R<String> r = R.ok("hello");
        assertThat(r.code()).isEqualTo(0);
        assertThat(r.data()).isEqualTo("hello");
        assertThat(r.success()).isTrue();
    }

    @Test
    void fail_carries_error_code_and_message() {
        R<Void> r = R.fail(CommonErrorCode.UNAUTHORIZED);
        assertThat(r.code()).isEqualTo(401);
        assertThat(r.message()).isEqualTo("未认证");
        assertThat(r.success()).isFalse();
        assertThat(r.data()).isNull();
    }

    @Test
    void business_exception_carries_error_code() {
        assertThatThrownBy(() -> {
                    throw new BusinessException(CommonErrorCode.FORBIDDEN);
                })
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).errorCode().code())
                .isEqualTo(403);
    }

    @Test
    void business_exception_allows_custom_message() {
        BusinessException e = new BusinessException(CommonErrorCode.BAD_REQUEST, "手机号格式不对");
        assertThat(e.getMessage()).isEqualTo("手机号格式不对");
        assertThat(e.errorCode().code()).isEqualTo(400);
    }

    @Test
    void request_user_exposes_identity_and_entitlement() {
        RequestUser u = new RequestUser(42L, "cus_abc", true);
        assertThat(u.id()).isEqualTo(42L);
        assertThat(u.customUserId()).isEqualTo("cus_abc");
        assertThat(u.isPro()).isTrue();
    }

    /** DomainEvent 不继承 ApplicationEvent（ADR-0013），所以它在没有 Spring 的情况下也能 new 出来 */
    @Test
    void domain_event_records_occurred_at_without_spring() {
        Instant before = Instant.now();
        TestEvent e = new TestEvent("payload");
        assertThat(e.occurredAt())
                .isBetween(before.minusSeconds(1), Instant.now().plusSeconds(1));
        assertThat(e.payload()).isEqualTo("payload");
    }

    @Test
    void page_data_empty_has_no_records() {
        PageData<String> page = PageData.empty(1, 20);
        assertThat(page.records()).isEmpty();
        assertThat(page.total()).isZero();
        assertThat(page.size()).isEqualTo(20);
    }

    static final class TestEvent extends DomainEvent {
        private final String payload;

        TestEvent(String payload) {
            this.payload = payload;
        }

        String payload() {
            return payload;
        }
    }
}
