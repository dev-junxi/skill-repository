package __BASE_PACKAGE__.component.dao.config;

import java.sql.Statement;
import java.util.Properties;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 慢 SQL 日志。
 *
 * <p><b>阈值默认 200ms 而不是 1 秒</b>——参考实现用了框架默认的 1 秒且从未调过，
 * 结果慢查询日志长期一片空白，让人误判"没有慢 SQL"。
 *
 * <p><b>注意这里只计语句执行时间，不含等连接池的时间。</b>连接池打满时请求卡在
 * acquire 上排队，这里的日志会干干净净而接口是慢的——那种情况要看 HikariCP 的
 * {@code hikaricp.connections.pending} 指标，不是看这条日志。
 *
 * <p><b>只打 SQL 模板，不打参数值</b>：参数里可能是用户内容。这与请求日志的脱敏
 * 是同一条边界——加密列的明文最常见的泄漏路径就是这类"为了好排查"的日志。
 */
@Intercepts({
    @Signature(
            type = StatementHandler.class,
            method = "query",
            args = {Statement.class, ResultHandler.class}),
    @Signature(
            type = StatementHandler.class,
            method = "update",
            args = {Statement.class})
})
public class SlowSqlInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(SlowSqlInterceptor.class);

    private long thresholdMillis = 200L;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long cost = System.currentTimeMillis() - start;
            if (cost >= thresholdMillis) {
                StatementHandler handler = (StatementHandler) invocation.getTarget();
                String sql = handler.getBoundSql().getSql().replaceAll("\\s+", " ");
                log.warn("slow sql cost={}ms sql={}", cost, sql);
            }
        }
    }

    @Override
    public void setProperties(Properties properties) {
        String value = properties.getProperty("thresholdMillis");
        if (value != null) {
            thresholdMillis = Long.parseLong(value);
        }
    }

    public void setThresholdMillis(long thresholdMillis) {
        this.thresholdMillis = thresholdMillis;
    }
}
