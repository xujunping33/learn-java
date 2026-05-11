package learn.java.ssmsocial.web;

import java.util.List;

import jakarta.servlet.http.HttpSession;
import learn.java.ssmsocial.auth.SessionKeys;
import learn.java.ssmsocial.model.Comment;
import learn.java.ssmsocial.service.CommentService;
import learn.java.ssmsocial.web.dto.CommentResponse;
import learn.java.ssmsocial.web.dto.CreateCommentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/posts")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping(
            path = "/{id}/comments",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(
            @PathVariable("id") long postId, @RequestBody CreateCommentRequest body, HttpSession session) {
        Object uid = session.getAttribute(SessionKeys.UID);
        if (!(uid instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "not logged in");
        }
        if (body == null) {
            throw new IllegalArgumentException("body is required");
        }
        Comment c = commentService.addComment(postId, userId, body.content());
        return toResponse(c);
    }

    @GetMapping(path = "/{id}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CommentResponse> list(@PathVariable("id") long postId) {
        return commentService.listByPostId(postId).stream().map(CommentController::toResponse).toList();
    }

    private static CommentResponse toResponse(Comment c) {
        return new CommentResponse(
                c.getId() == null ? 0 : c.getId(),
                c.getPostId() == null ? 0 : c.getPostId(),
                c.getUserId() == null ? 0 : c.getUserId(),
                c.getAuthorUsername(),
                c.getContent(),
                c.getCreatedAt());
    }
}

