package learn.java.ssmsocial.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import learn.java.ssmsocial.web.dto.ApiErrorBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorBody> handleBadRequest(IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "bad request" : ex.getMessage();
        // very small convenience: allow throwing IllegalArgumentException("... not found")
        if (msg.toLowerCase().contains("not found")) {
            return body(HttpStatus.NOT_FOUND, new ApiErrorBody("NOT_FOUND", msg));
        }
        return body(HttpStatus.BAD_REQUEST, new ApiErrorBody("BAD_REQUEST", msg));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorBody> handleStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String msg = ex.getReason() == null ? status.getReasonPhrase() : ex.getReason();
        return body(status, new ApiErrorBody(status.name(), msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorBody> handleAny(Exception ex) {
        log.error("Unhandled exception", ex);
        return body(
                HttpStatus.INTERNAL_SERVER_ERROR,
                new ApiErrorBody("INTERNAL_ERROR", "unexpected server error"));
    }

    private static ResponseEntity<ApiErrorBody> body(HttpStatus status, ApiErrorBody err) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(err);
    }
}

