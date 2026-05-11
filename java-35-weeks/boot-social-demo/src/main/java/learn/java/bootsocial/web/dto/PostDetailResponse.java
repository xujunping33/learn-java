package learn.java.bootsocial.web.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "帖子详情：含聚合 `comments` 与 `likeCount`")
public record PostDetailResponse(
        long id,
        long userId,
        String authorUsername,
        long likeCount,
        String title,
        String content,
        @Schema(description = "封面短期可访问 URL（presigned GET）；无封面则为 null") String coverUrl,
        String createdAt,
        List<CommentResponse> comments) {}
