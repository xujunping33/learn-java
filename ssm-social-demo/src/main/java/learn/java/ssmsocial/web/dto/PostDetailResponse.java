package learn.java.ssmsocial.web.dto;

import java.util.List;

public record PostDetailResponse(
        long id,
        long userId,
        String authorUsername,
        long likeCount,
        String title,
        String content,
        String createdAt,
        List<CommentResponse> comments) {}

