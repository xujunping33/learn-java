package learn.java.bootsocial.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import learn.java.bootsocial.mq.MqFailureSwitch;
import learn.java.bootsocial.web.dto.ApiResult;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dev MQ", description = "仅 dev：MQ 失败开关")
@RestController
@RequestMapping("/api/dev/mq")
@Profile({"dev", "docker"})
public class DevMqFailureController {

    private final MqFailureSwitch mqFailureSwitch;

    public DevMqFailureController(MqFailureSwitch mqFailureSwitch) {
        this.mqFailureSwitch = mqFailureSwitch;
    }

    @Operation(summary = "查看模拟失败开关状态")
    @GetMapping("/failure")
    public ApiResult<Boolean> status() {
        return ApiResult.ok(mqFailureSwitch.isEnabled());
    }

    @Operation(summary = "设置模拟失败开关（true=开启，false=关闭）")
    @PostMapping("/failure")
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<Boolean> set(@RequestParam boolean enabled) {
        mqFailureSwitch.setEnabled(enabled);
        return ApiResult.ok(mqFailureSwitch.isEnabled());
    }
}

