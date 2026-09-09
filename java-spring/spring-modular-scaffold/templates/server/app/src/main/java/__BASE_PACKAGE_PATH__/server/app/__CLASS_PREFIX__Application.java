package __BASE_PACKAGE__.server.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 唯一组装点。
 *
 * <p>扫描 {@code __BASE_PACKAGE__} 覆盖所有组件与业务模块——模块自治由 Maven 编译期依赖边界保证，
 * 不需要手写工厂函数做依赖注入。
 */
@SpringBootApplication(scanBasePackages = "__BASE_PACKAGE__")
public class __CLASS_PREFIX__Application {

    public static void main(String[] args) {
        SpringApplication.run(__CLASS_PREFIX__Application.class, args);
    }
}
