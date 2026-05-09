package learn.java.bootsocial.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dev：MinIO smoke — 写入测试对象 + presigned GET URL")
public record StorageSmokeResponse(
        @Schema(description = "bucket 名") String bucket,
        @Schema(description = "object key") String objectKey,
        @Schema(description = "presigned GET（短时有效）") String presignedUrl) {}
