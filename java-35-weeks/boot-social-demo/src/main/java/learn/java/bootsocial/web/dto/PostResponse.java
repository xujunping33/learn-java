package learn.java.bootsocial.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "帖子摘要（列表项）")
public record PostResponse(
        long id,
        long userId,
        String authorUsername,
        long likeCount,
        String title,
        String content,
        String createdAt) {}
