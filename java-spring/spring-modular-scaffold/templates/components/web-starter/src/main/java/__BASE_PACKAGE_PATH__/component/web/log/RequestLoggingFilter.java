package __BASE_PACKAGE__.component.web.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * 统一记录请求/响应。
 *
 * <p><b>铁律：业务代码禁止再打印请求体/响应体</b>——本过滤器已完整记录，重复打印既是噪音，
 * 也绕过了这里的脱敏（ADR-0006）。
 *
 * <p>日志级别按状态码分流：2xx/3xx → info，4xx → warn，5xx → error。
 */
public class RequestLoggingFilter extends OncePerRequestFilter implements Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private final SensitiveDataMasker masker;

    public RequestLoggingFilter(SensitiveDataMasker masker) {
        this.masker = masker;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper resp = new ContentCachingResponseWrapper(response);
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(req, resp);
        } finally {
            logExchange(req, resp, System.currentTimeMillis() - start);
            resp.copyBodyToResponse();
        }
    }

    private void logExchange(ContentCachingRequestWrapper req, ContentCachingResponseWrapper resp, long cost) {
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        String fullUri = query == null ? uri : uri + "?" + query;
        int status = resp.getStatus();

        String reqBody = bodyOf(req.getContentAsByteArray(), uri);
        String respBody = isStream(resp) ? "<stream>" : bodyOf(resp.getContentAsByteArray(), uri);

        String msg = "{} {} status={} cost={}ms req={} resp={}";
        if (status >= 500) {
            log.error(msg, req.getMethod(), fullUri, status, cost, reqBody, respBody);
        } else if (status >= 400) {
            log.warn(msg, req.getMethod(), fullUri, status, cost, reqBody, respBody);
        } else {
            log.info(msg, req.getMethod(), fullUri, status, cost, reqBody, respBody);
        }
    }

    private boolean isStream(ContentCachingResponseWrapper resp) {
        String contentType = resp.getContentType();
        return contentType != null && contentType.startsWith("text/event-stream");
    }

    private String bodyOf(byte[] raw, String uri) {
        if (masker.shouldRedactPath(uri)) {
            return "<redacted-by-path>";
        }
        if (raw == null || raw.length == 0) {
            return "";
        }
        return masker.mask(new String(raw, StandardCharsets.UTF_8));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
