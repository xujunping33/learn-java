package learn.java.ssmsocial.web;

import java.util.List;

import jakarta.servlet.http.HttpSession;
import learn.java.ssmsocial.auth.SessionKeys;
import learn.java.ssmsocial.model.Comment;
import learn.java.ssmsocial.model.Post;
import learn.java.ssmsocial.service.CommentService;
import learn.java.ssmsocial.service.PostService;
import learn.java.ssmsocial.web.dto.CommentResponse;
import learn.java.ssmsocial.web.dto.CreatePostRequest;
import learn.java.ssmsocial.web.dto.PostDetailResponse;
import learn.java.ssmsocial.web.dto.PostResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    private final PostService postService;
    private final CommentService commentService;

    public PostController(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(@RequestBody CreatePostRequest body, HttpSession session) {
        Object uid = session.getAttribute(SessionKeys.UID);
        if (!(uid instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "not logged in");
        }
        if (body == null) {
            throw new IllegalArgumentException("body is required");
        }
        Post created = postService.createPost(userId, body.title(), body.content());
        log.info("post created id={} userId={}", created.getId(), userId);
        return toResponse(created);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PostResponse> list(
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit,
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset) {
        return postService.listPosts(limit, offset).stream().map(PostController::toResponse).toList();
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object detail(
            @PathVariable("id") long id,
            @RequestParam(name = "includeComments", required = false, defaultValue = "false")
                    boolean includeComments) {
        Post p = postService.getPostDetail(id);
        if (p == null) {
            throw new IllegalArgumentException("post not found");
        }
        if (!includeComments) {
            return toResponse(p);
        }
        List<CommentResponse> comments =
                commentService.listByPostId(id).stream().map(PostController::toCommentResponse).toList();
        long likeCount = p.getLikeCount() == null ? 0 : p.getLikeCount();
        return new PostDetailResponse(
                p.getId() == null ? 0 : p.getId(),
                p.getUserId() == null ? 0 : p.getUserId(),
                p.getAuthorUsername(),
                likeCount,
                p.getTitle(),
                p.getContent(),
                p.getCreatedAt(),
                comments);
    }

    private static PostResponse toResponse(Post p) {
        long likeCount = p.getLikeCount() == null ? 0 : p.getLikeCount();
        return new PostResponse(
                p.getId() == null ? 0 : p.getId(),
                p.getUserId() == null ? 0 : p.getUserId(),
                p.getAuthorUsername(),
                likeCount,
                p.getTitle(),
                p.getContent(),
                p.getCreatedAt());
    }

    private static CommentResponse toCommentResponse(Comment c) {
        return new CommentResponse(
                c.getId() == null ? 0 : c.getId(),
                c.getPostId() == null ? 0 : c.getPostId(),
                c.getUserId() == null ? 0 : c.getUserId(),
                c.getAuthorUsername(),
                c.getContent(),
                c.getCreatedAt());
    }
}

