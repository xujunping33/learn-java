package learn.java.bootsocial.mq;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "MQ 测试消息（W29 Day200）")
public record MqTestMessage(
        @Schema(description = "消息 id（用于日志关联）") String id,
        @Schema(description = "消息内容") String text,
        @Schema(description = "发送时间（ISO）") String occurredAt) {}

