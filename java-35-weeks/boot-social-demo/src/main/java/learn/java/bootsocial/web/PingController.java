package learn.java.bootsocial.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import learn.java.bootsocial.web.dto.ApiResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Ping", description = "健康检查（全 profile）")
@RestController
public class PingController {

    @Operation(summary = "连通性探测", description = "`ApiResult` 包布尔 data")
    @GetMapping(path = "/api/ping", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<Boolean> ping() {
        return ApiResult.ok(true);
    }
}

