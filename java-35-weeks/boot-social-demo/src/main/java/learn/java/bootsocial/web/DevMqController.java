package learn.java.bootsocial.web;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import learn.java.bootsocial.mq.MqConfiguration;
import learn.java.bootsocial.mq.MqTestMessage;
import learn.java.bootsocial.web.dto.ApiResult;
import learn.java.bootsocial.web.dto.MqTestRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dev MQ", description = "仅 dev：MQ 冒烟")
@RestController
@RequestMapping("/api/dev")
@Profile({"dev", "docker"})
public class DevMqController {

    private final RabbitTemplate rabbitTemplate;

    public DevMqController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Operation(summary = "发送一条 MQ 测试消息")
    @PostMapping(path = "/mq-test", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResult<MqTestMessage> mqTest(@Valid @RequestBody MqTestRequest body) {
        MqTestMessage msg = new MqTestMessage(UUID.randomUUID().toString(), body.text(), Instant.now().toString());
        rabbitTemplate.convertAndSend(MqConfiguration.TEST_EXCHANGE, MqConfiguration.TEST_ROUTING_KEY, msg);
        return ApiResult.ok(msg);
    }
}

