package learn.java.ssmsocial.web.dto;

public record CommentResponse(
        long id,
        long postId,
        long userId,
        String authorUsername,
        String content,
        String createdAt) {}

