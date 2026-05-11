package learn.java.bootsocial.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "发送一条 MQ 测试消息")
public record MqTestRequest(@NotBlank @Schema(description = "消息内容") String text) {}

