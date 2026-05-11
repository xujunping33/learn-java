package learn.java.oa.api;

/** 业务/客户端错误：由 {@link BaseJsonServlet} 转为统一 JSON。 */
public class ApiException extends RuntimeException {

    private final int httpStatus;
    private final int bizCode;

    public ApiException(int httpStatus, int bizCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.bizCode = bizCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public int getBizCode() {
        return bizCode;
    }
}
