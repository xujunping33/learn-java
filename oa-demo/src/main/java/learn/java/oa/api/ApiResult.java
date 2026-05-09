package learn.java.oa.api;

/** 统一 JSON：{@code { "code": number, "message": string, "data": any }}} */
public class ApiResult {

    public final int code;
    public final String message;
    public final Object data;

    public ApiResult(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
