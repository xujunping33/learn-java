package learn.java.bootsocial.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "单条评论")
public record CommentResponse(
        long id,
        long postId,
        long userId,
        String authorUsername,
        String content,
        String createdAt) {}
