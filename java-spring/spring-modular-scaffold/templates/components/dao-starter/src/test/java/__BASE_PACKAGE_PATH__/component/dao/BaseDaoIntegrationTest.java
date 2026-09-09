package __BASE_PACKAGE__.component.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 集成测试基线：每次跑起一个干净的 MySQL 实例。
 *
 * <p><b>绝不连共享测试库。</b>参考实现的前身为此付出过六次代价——共享库上的残留数据会让
 * "查集合 + contains"型断言悄悄变成恒真，`--test-threads=1` 会掩盖并发缺陷，
 * 本地起的 mysqld 顶替端口会让"全绿"报告完全是假的。这些失效有个共同点：
 * <b>不会被任何现有测试发现</b>。换成每次起干净实例后，这一整类问题不复存在。
 *
 * <p>只用 {@code @ServiceConnection} 自动推导连接属性，不再叠加
 * {@code @DynamicPropertySource} 手工设一遍——两者职责重叠，且对 url 的推导若不一致
 * 会产生难查的覆盖顺序问题。
 */
@Testcontainers
@SpringBootTest(classes = BaseDaoIntegrationTest.TestApp.class)
class BaseDaoIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("__ARTIFACT_PREFIX___test")
            .withUsername("__ARTIFACT_PREFIX__")
            .withPassword("__ARTIFACT_PREFIX__");

    @SpringBootApplication
    static class TestApp {}

    @Autowired
    private DataSource dataSource;

    @Test
    void container_starts_and_datasource_is_wired() throws Exception {
        try (Connection conn = dataSource.getConnection();
                Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE demo (id BIGINT PRIMARY KEY, name VARCHAR(32))");
            st.execute("INSERT INTO demo VALUES (1, 'alice')");
            try (ResultSet rs = st.executeQuery("SELECT name FROM demo WHERE id = 1")) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("name")).isEqualTo("alice");
            }
        }
    }

    /** 证明连的是隔离实例而非共享库：端口是随机映射的，绝不是 3307 */
    @Test
    void database_is_an_isolated_container_not_the_shared_one() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertThat(conn.getCatalog()).isEqualTo("__ARTIFACT_PREFIX___test");
        }
        assertThat(mysql.getJdbcUrl())
                .as("Testcontainers 用随机端口，出现 3307 说明连到了共享测试库")
                .doesNotContain(":3307/");
    }
}
