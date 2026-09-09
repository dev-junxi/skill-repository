package __BASE_PACKAGE__.server.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AppSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Environment environment;

    @Test
    void context_loads_and_health_endpoint_returns_unified_response() throws Exception {
        mockMvc.perform(get("/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    /** traceId 必须回写响应头，且客户端传入时原样透传 */
    @Test
    void trace_id_is_returned_and_propagated() throws Exception {
        mockMvc.perform(get("/v1/health")).andExpect(header().exists("x-trace-id"));

        mockMvc.perform(get("/v1/health").header("x-trace-id", "client-trace-1"))
                .andExpect(header().string("x-trace-id", "client-trace-1"));
    }

    /** 虚拟线程必须真的开着——ADR-0004 的前提 */
    @Test
    void virtual_threads_are_enabled() {
        assertThat(environment.getProperty("spring.threads.virtual.enabled", Boolean.class))
                .isTrue();
    }

    /** 优雅停机必须配置——多实例滚动发布的前提（ADR-0011） */
    @Test
    void graceful_shutdown_is_configured() {
        assertThat(environment.getProperty("server.shutdown")).isEqualTo("graceful");
    }
}
