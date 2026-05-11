package learn.java.bootsocial.mq;

/**
 * 通知落库幂等键（与 {@code notifications.dedup_key UNIQUE} 一致，W30 Day207）。
 * <p>
 * 规则需与 Outbox 发布的事件类型一一对应；重复投递时 {@code INSERT IGNORE} 不插第二行。
 */
public final class DedupKeys {
    private DedupKeys() {}

    /** {@code COMMENT_CREATED}：同一条评论只产生一条通知。 */
    public static String commentCreated(long commentId) {
        return "comment:" + commentId;
    }

    /** {@code POST_LIKED}：同一用户对同一帖子的点赞通知只落一条（幂等点赞与此无关）。 */
    public static String postLiked(long postId, long actorId) {
        return "like:" + postId + ":" + actorId;
    }
}
