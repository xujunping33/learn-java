package learn.java.bootsocial.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "成功响应包络：`ok` 固定 true，`data` 为具体负载")
public record ApiResult<T>(boolean ok, T data) {
    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(true, data);
    }
}
