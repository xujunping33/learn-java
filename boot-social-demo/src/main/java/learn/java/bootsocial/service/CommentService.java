package learn.java.bootsocial.service;

import java.util.List;
import java.time.Instant;
import java.util.UUID;

import learn.java.bootsocial.cache.PostDetailCache;
import learn.java.bootsocial.mapper.CommentMapper;
import learn.java.bootsocial.mq.MqConfiguration;
import learn.java.bootsocial.mq.DedupKeys;
import learn.java.bootsocial.mq.PostEvents;
import learn.java.bootsocial.model.Comment;
import learn.java.bootsocial.model.Post;
import learn.java.bootsocial.web.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile({"dev", "docker"})
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentMapper commentMapper;
    private final PostService postService;
    private final PostDetailCache postDetailCache;
    private final OutboxService outboxService;

    public CommentService(
            CommentMapper commentMapper,
            PostService postService,
            PostDetailCache postDetailCache,
            OutboxService outboxService) {
        this.commentMapper = commentMapper;
        this.postService = postService;
        this.postDetailCache = postDetailCache;
        this.outboxService = outboxService;
    }

    @Transactional
    public Comment addComment(long postId, long userId, String content) {
        if (userId <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "userId is required");
        }
        if (postId <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "postId is required");
        }
        String c = content == null ? "" : content.trim();
        if (c.isEmpty()) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "content is required");
        }
        if (c.length() > 2000) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "content too long");
        }

        Post p = postService.getPostDetail(postId);
        if (p == null) {
            throw new BizException(HttpStatus.NOT_FOUND, "NOT_FOUND", "post not found");
        }

        Comment cm = new Comment();
        cm.setPostId(postId);
        cm.setUserId(userId);
        cm.setContent(c);
        commentMapper.insertComment(cm);

        // W27 Day185: comment changes detail aggregation -> evict
        postDetailCache.evict(postId);

        // W29 Day201: publish notify event (async)
        Long ownerId = p.getUserId();
        if (ownerId != null && ownerId > 0 && ownerId != userId && cm.getId() != null) {
            String preview = c.length() <= 80 ? c : c.substring(0, 80);
            PostEvents.CommentCreated evt =
                    new PostEvents.CommentCreated(
                            UUID.randomUUID().toString(),
                            Instant.now().toString(),
                            postId,
                            cm.getId(),
                            userId,
                            ownerId,
                            preview);
            log.info(
                    "notify_chain_src COMMENT_CREATED eventId={} postId={} commentId={} actorId={} ownerId={} dedupKey={}",
                    evt.eventId(),
                    postId,
                    cm.getId(),
                    userId,
                    ownerId,
                    DedupKeys.commentCreated(cm.getId()));
            outboxService.enqueue(MqConfiguration.NOTIFY_EXCHANGE, MqConfiguration.RK_COMMENT_CREATED, evt);
        }

        return commentMapper.listCommentsByPostId(postId).stream()
                .filter(x -> x.getId() != null && x.getId().equals(cm.getId()))
                .findFirst()
                .orElse(cm);
    }

    public List<Comment> listByPostId(long postId) {
        if (postId <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "postId is required");
        }
        if (postService.getPostDetail(postId) == null) {
            throw new BizException(HttpStatus.NOT_FOUND, "NOT_FOUND", "post not found");
        }
        return commentMapper.listCommentsByPostId(postId);
    }
}
