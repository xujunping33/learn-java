package learn.java.bootsocial.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "当前用户信息")
public record MeResponse(long id, String username) {}

