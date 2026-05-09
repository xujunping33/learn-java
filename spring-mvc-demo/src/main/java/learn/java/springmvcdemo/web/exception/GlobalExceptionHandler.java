package learn.java.springmvcdemo.web.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import learn.java.springmvcdemo.web.dto.ApiErrorBody;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorBody> handleApi(ApiException ex) {
        return body(ex.getStatus(), new ApiErrorBody(ex.getApiCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorBody> handleValidation(MethodArgumentNotValidException ex) {
        String detail =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                        .collect(Collectors.joining("; "));
        HttpStatus bad = HttpStatus.BAD_REQUEST;
        return body(bad, new ApiErrorBody("VALIDATION_FAILED", detail));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorBody> handleBadJson(HttpMessageNotReadableException ignored) {
        return body(
                HttpStatus.BAD_REQUEST,
                new ApiErrorBody("BAD_REQUEST", "invalid or missing JSON body"));
    }

    /** 不向客户端透出堆栈与内部异常类名（生产可再打日志 correlationId）。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorBody> handleAny(Exception ignored) {
        return body(
                HttpStatus.INTERNAL_SERVER_ERROR,
                new ApiErrorBody("INTERNAL_ERROR", "unexpected server error"));
    }

    private static ResponseEntity<ApiErrorBody> body(HttpStatus status, ApiErrorBody err) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(err);
    }
}
