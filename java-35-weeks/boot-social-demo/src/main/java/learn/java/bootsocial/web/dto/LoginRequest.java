package learn.java.bootsocial.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "登录请求体")
public record LoginRequest(
        @NotBlank @Size(max = 64) String username,
        @NotBlank @Size(min = 6, max = 100) String password) {}

