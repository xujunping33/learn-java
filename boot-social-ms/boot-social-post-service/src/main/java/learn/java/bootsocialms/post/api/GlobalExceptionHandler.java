package learn.java.bootsocialms.post.api;

import feign.FeignException;
import learn.java.bootsocialms.post.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiErrorResponse> handleFeign(FeignException ex) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        if (status.is5xxServerError()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ApiErrorResponse("DOWNSTREAM_UNAVAILABLE", "Downstream service unavailable"));
        }
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse("DOWNSTREAM_ERROR", "Downstream error"));
    }
}

