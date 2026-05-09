package learn.java.bootsocial.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import learn.java.bootsocial.storage.CoverDbFailureSwitch;
import learn.java.bootsocial.web.dto.ApiResult;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dev Storage", description = "仅 dev：对象存储失败开关")
@RestController
@RequestMapping("/api/dev/storage")
@Profile({"dev", "docker"})
public class DevStorageFailureController {

    private final CoverDbFailureSwitch coverDbFailureSwitch;

    public DevStorageFailureController(CoverDbFailureSwitch coverDbFailureSwitch) {
        this.coverDbFailureSwitch = coverDbFailureSwitch;
    }

    @Operation(summary = "查看封面写库失败开关状态")
    @GetMapping("/cover-db-failure")
    public ApiResult<Boolean> status() {
        return ApiResult.ok(coverDbFailureSwitch.isEnabled());
    }

    @Operation(summary = "设置封面写库失败开关（true=开启，false=关闭）")
    @PostMapping("/cover-db-failure")
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<Boolean> set(@RequestParam boolean enabled) {
        coverDbFailureSwitch.setEnabled(enabled);
        return ApiResult.ok(coverDbFailureSwitch.isEnabled());
    }
}

