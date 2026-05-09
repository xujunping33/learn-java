package learn.java.bootsocial.web.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import learn.java.bootsocial.web.dto.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            details.put(fe.getField(), fe.getDefaultMessage());
        }
        return json(HttpStatus.BAD_REQUEST, new ApiError("VALIDATION_FAILED", "validation failed", details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            details.put(v.getPropertyPath().toString(), v.getMessage());
        }
        return json(HttpStatus.BAD_REQUEST, new ApiError("VALIDATION_FAILED", "validation failed", details));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "bad request" : ex.getMessage();
        return json(HttpStatus.BAD_REQUEST, ApiError.simple("BAD_REQUEST", msg));
    }

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiError> handleBiz(BizException ex) {
        HttpStatus status = ex.status();
        return json(status, ApiError.simple(ex.code(), ex.getMessage()));
    }

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ApiError> handleNotLogin(NotLoginException ex) {
        NotLoginMapping mapped = mapNotLogin(ex);
        return json(HttpStatus.UNAUTHORIZED, ApiError.simple(mapped.code(), mapped.message()));
    }

    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<ApiError> handleNotPermission(NotPermissionException ex) {
        return json(HttpStatus.FORBIDDEN, ApiError.simple("FORBIDDEN", "forbidden"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex) {
        return json(HttpStatus.NOT_FOUND, ApiError.simple("NOT_FOUND", "not found"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String msg = ex.getReason() == null ? status.getReasonPhrase() : ex.getReason();
        return json(status, ApiError.simple(status.name(), msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAny(Exception ex) {
        log.error("Unhandled exception", ex);
        return json(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiError.simple("INTERNAL_ERROR", "unexpected server error"));
    }

    private static ResponseEntity<ApiError> json(HttpStatus status, ApiError body) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    private static NotLoginMapping mapNotLogin(NotLoginException ex) {
        String type = ex == null ? null : ex.getType();
        if (NotLoginException.NOT_TOKEN.equals(type)) {
            return new NotLoginMapping("AUTH_REQUIRED", "not logged in");
        }
        if (NotLoginException.TOKEN_TIMEOUT.equals(type)) {
            return new NotLoginMapping("AUTH_EXPIRED", "token expired");
        }
        if (NotLoginException.INVALID_TOKEN.equals(type)) {
            return new NotLoginMapping("AUTH_INVALID_TOKEN", "invalid token");
        }
        if (NotLoginException.BE_REPLACED.equals(type)) {
            return new NotLoginMapping("AUTH_EXPIRED", "login replaced");
        }
        if (NotLoginException.KICK_OUT.equals(type)) {
            return new NotLoginMapping("AUTH_EXPIRED", "kicked out");
        }
        return new NotLoginMapping("AUTH_REQUIRED", "not logged in");
    }

    private record NotLoginMapping(String code, String message) {}
}

