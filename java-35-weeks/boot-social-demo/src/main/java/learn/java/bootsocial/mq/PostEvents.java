package learn.java.bootsocial.mq;

import io.swagger.v3.oas.annotations.media.Schema;

public final class PostEvents {
    private PostEvents() {}

    @Schema(description = "评论创建事件（W29 Day201）")
    public record CommentCreated(
            String eventId,
            String occurredAt,
            long postId,
            long commentId,
            long actorId,
            long ownerId,
            String commentPreview) {}

    @Schema(description = "点赞事件（W29 Day201）")
    public record PostLiked(
            String eventId,
            String occurredAt,
            long postId,
            long actorId,
            long ownerId) {}
}

