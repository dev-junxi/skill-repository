package __BASE_PACKAGE__.server.app;

import __BASE_PACKAGE__.contract.response.R;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 给负载均衡用的轻量探活端点。actuator 的 /actuator/health 只在内网暴露。 */
@RestController
public class HealthController {

    @GetMapping("/v1/health")
    public R<Map<String, String>> health() {
        return R.ok(Map.of("status", "UP"));
    }
}
