package learn.java.bootsocial.service;

import java.util.List;

import learn.java.bootsocial.cache.PostDetailCache;
import learn.java.bootsocial.cache.PostDetailCache.Peek;
import learn.java.bootsocial.model.Post;
import learn.java.bootsocial.web.dto.CommentResponse;
import learn.java.bootsocial.web.dto.PostDetailResponse;
import learn.java.bootsocial.web.exception.BizException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Profile({"dev", "docker"})
public class PostDetailService {

    private final PostService postService;
    private final CommentService commentService;
    private final PostDetailCache postDetailCache;

    public PostDetailService(PostService postService, CommentService commentService, PostDetailCache postDetailCache) {
        this.postService = postService;
        this.commentService = commentService;
        this.postDetailCache = postDetailCache;
    }

    public PostDetailResponse get(long id) {
        return switch (postDetailCache.peek(id)) {
            case Peek.Hit(var body) -> body;
            case Peek.AbsentMarker() -> throw new BizException(HttpStatus.NOT_FOUND, "NOT_FOUND", "post not found");
            case Peek.Miss() -> loadAndCache(id);
        };
    }

    private PostDetailResponse loadAndCache(long id) {
        Post p = postService.getPostDetail(id);
        if (p == null) {
            postDetailCache.putAbsent(id);
            throw new BizException(HttpStatus.NOT_FOUND, "NOT_FOUND", "post not found");
        }
        List<CommentResponse> comments =
                commentService.listByPostId(id).stream().map(PostDetailService::toCommentResponse).toList();
        long likeCount = longOrZero(p.getLikeCount());
        PostDetailResponse resp =
                new PostDetailResponse(
                        longOrZero(p.getId()),
                        longOrZero(p.getUserId()),
                        p.getAuthorUsername(),
                        likeCount,
                        p.getTitle(),
                        p.getContent(),
                        p.getCreatedAt(),
                        comments);
        postDetailCache.put(id, resp);
        return resp;
    }

    private static CommentResponse toCommentResponse(learn.java.bootsocial.model.Comment c) {
        return new CommentResponse(
                longOrZero(c.getId()),
                longOrZero(c.getPostId()),
                longOrZero(c.getUserId()),
                c.getAuthorUsername(),
                c.getContent(),
                c.getCreatedAt());
    }

    private static long longOrZero(Long v) {
        return switch (v) {
            case null -> 0L;
            case Long n -> n;
        };
    }
}

