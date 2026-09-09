package __BASE_PACKAGE__.component.web.error;

import __BASE_PACKAGE__.contract.error.BusinessException;
import __BASE_PACKAGE__.contract.error.CommonErrorCode;
import __BASE_PACKAGE__.contract.response.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<Void>> handleBusiness(BusinessException e) {
        log.warn("business error code={}", e.errorCode().code());
        return ResponseEntity.ok(R.fail(e.errorCode(), e.getMessage()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<R<Void>> handleValidation(BindException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .orElse(CommonErrorCode.BAD_REQUEST.message());
        log.warn("validation failed: {}", detail);
        return ResponseEntity.ok(R.fail(CommonErrorCode.BAD_REQUEST, detail));
    }

    /** 兜底。只记异常本身，不把请求内容拼进日志——那是加密边界最常见的泄漏路径。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleUnexpected(Exception e) {
        log.error("unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(R.fail(CommonErrorCode.INTERNAL_ERROR));
    }
}
