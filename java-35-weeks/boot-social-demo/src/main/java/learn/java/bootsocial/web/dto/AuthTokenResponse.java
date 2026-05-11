package learn.java.bootsocial.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登录/注册成功后的 token 响应")
public record AuthTokenResponse(
        @Schema(description = "用户 id") long id,
        @Schema(description = "用户名") String username,
        @Schema(description = "token header 名（通常 Authorization）") String tokenName,
        @Schema(description = "token 值（不含 Bearer 前缀）") String tokenValue,
        @Schema(description = "token 前缀（通常 Bearer）") String tokenPrefix) {}

