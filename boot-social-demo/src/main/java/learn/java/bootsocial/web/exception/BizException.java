package learn.java.bootsocial.web.exception;

import org.springframework.http.HttpStatus;

public class BizException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public BizException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }
}
