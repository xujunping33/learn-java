package learn.java.springmvcdemo.web.exception;

import org.springframework.http.HttpStatus;

/** Day158：可控业务/API 异常，由 {@link GlobalExceptionHandler} 统一转为 {@link learn.java.springmvcdemo.web.dto.ApiErrorBody}。 */
public final class ApiException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    private ApiException(String code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getApiCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException notFound(String message) {
        return new ApiException("NOT_FOUND", HttpStatus.NOT_FOUND, message);
    }

    /** 占位：后续可扩展 VALIDATION_*、BUSINESS_RULE 等稳定 code。 */
    public static ApiException badRequest(String message) {
        return new ApiException("BAD_REQUEST", HttpStatus.BAD_REQUEST, message);
    }
}
