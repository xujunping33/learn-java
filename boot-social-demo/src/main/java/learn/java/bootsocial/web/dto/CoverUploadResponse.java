package learn.java.bootsocial.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "上传封面成功响应")
public record CoverUploadResponse(
        @Schema(description = "对象 key（落库）") String objectKey,
        @Schema(description = "短期可访问 URL（presigned GET）") String coverUrl) {}

