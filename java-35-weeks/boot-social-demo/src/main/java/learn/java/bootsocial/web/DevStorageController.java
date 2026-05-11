package learn.java.bootsocial.web;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import learn.java.bootsocial.service.StorageService;
import learn.java.bootsocial.web.dto.ApiResult;
import learn.java.bootsocial.web.dto.StorageSmokeResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仅 dev：验证 MinIO + S3 SDK + presigned GET（W31 Day213）。
 */
@Tag(name = "Dev Storage", description = "仅 dev：对象存储冒烟")
@RestController
@RequestMapping("/api/dev")
@Profile({"dev", "docker"})
@ConditionalOnBean(StorageService.class)
public class DevStorageController {

    private final StorageService storageService;

    public DevStorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @Operation(summary = "MinIO smoke：put 小文件并返回 presigned GET")
    @PostMapping(path = "/storage/smoke", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<StorageSmokeResponse> smoke() {
        String key = "smoke/" + UUID.randomUUID() + ".txt";
        String body = "boot-social minio smoke\n";
        storageService.putObject(key, body.getBytes(StandardCharsets.UTF_8), MediaType.TEXT_PLAIN_VALUE);
        String url = storageService.presignedGetUrl(key);
        return ApiResult.ok(new StorageSmokeResponse(storageService.getBucket(), key, url));
    }
}
