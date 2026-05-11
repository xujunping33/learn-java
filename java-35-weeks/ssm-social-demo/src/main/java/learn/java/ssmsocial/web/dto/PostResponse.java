package learn.java.ssmsocial.web.dto;

public record PostResponse(
        long id,
        long userId,
        String authorUsername,
        long likeCount,
        String title,
        String content,
        String createdAt) {}

