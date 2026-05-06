package learn.java.bootsocial.web.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/** 分页列表统一结构：嵌在 {@link ApiResult} 的 {@code data} 中。 */
@Schema(description = "分页：`items`、`total`、`page`、`size`")
public record PageResult<T>(List<T> items, long total, long page, long size) {}
