package learn.java.bootsocial.web.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "错误体：`VALIDATION_FAILED` 时可有 `details` 字段校验说明")
public record ApiError(String code, String message, Map<String, Object> details) {
    public static ApiError simple(String code, String message) {
        return new ApiError(code, message, null);
    }
}

