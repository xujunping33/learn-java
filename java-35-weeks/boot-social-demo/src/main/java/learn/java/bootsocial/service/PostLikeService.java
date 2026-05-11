package learn.java.bootsocial.service;

import java.time.Instant;
import java.util.UUID;

import learn.java.bootsocial.cache.PostDetailCache;
import learn.java.bootsocial.mapper.PostLikeMapper;
import learn.java.bootsocial.mq.MqConfiguration;
import learn.java.bootsocial.mq.DedupKeys;
import learn.java.bootsocial.mq.PostEvents;
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
public class PostLikeService {

    private static final Logger log = LoggerFactory.getLogger(PostLikeService.class);

    private final PostLikeMapper postLikeMapper;
    private final PostService postService;
    private final PostDetailCache postDetailCache;
    private final OutboxService outboxService;

    public PostLikeService(
            PostLikeMapper postLikeMapper,
            PostService postService,
            PostDetailCache postDetailCache,
            OutboxService outboxService) {
        this.postLikeMapper = postLikeMapper;
        this.postService = postService;
        this.postDetailCache = postDetailCache;
        this.outboxService = outboxService;
    }

    @Transactional
    public void like(long postId, long userId) {
        if (userId <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "userId is required");
        }
        if (postId <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "postId is required");
        }
        Post p = postService.getPostDetail(postId);
        if (p == null) {
            throw new BizException(HttpStatus.NOT_FOUND, "NOT_FOUND", "post not found");
        }
        int rows = postLikeMapper.insertLike(postId, userId);
        // W27 Day185: likeCount changes -> evict
        postDetailCache.evict(postId);

        // W29 Day201: publish notify event only when inserted
        Long ownerId = p.getUserId();
        if (rows > 0 && ownerId != null && ownerId > 0 && ownerId != userId) {
            PostEvents.PostLiked evt =
                    new PostEvents.PostLiked(
                            UUID.randomUUID().toString(),
                            Instant.now().toString(),
                            postId,
                            userId,
                            ownerId);
            log.info(
                    "notify_chain_src POST_LIKED eventId={} postId={} actorId={} ownerId={} dedupKey={}",
                    evt.eventId(),
                    postId,
                    userId,
                    ownerId,
                    DedupKeys.postLiked(postId, userId));
            outboxService.enqueue(MqConfiguration.NOTIFY_EXCHANGE, MqConfiguration.RK_POST_LIKED, evt);
        }
    }

    @Transactional
    public void unlike(long postId, long userId) {
        if (userId <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "userId is required");
        }
        if (postId <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "postId is required");
        }
        postLikeMapper.deleteLike(postId, userId);
        // W27 Day185: likeCount changes -> evict
        postDetailCache.evict(postId);
    }
}
