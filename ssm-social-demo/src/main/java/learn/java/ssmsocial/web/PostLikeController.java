package learn.java.ssmsocial.web;

import jakarta.servlet.http.HttpSession;
import learn.java.ssmsocial.auth.SessionKeys;
import learn.java.ssmsocial.service.PostLikeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/posts")
public class PostLikeController {

    private final PostLikeService postLikeService;

    public PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
    }

    @PostMapping("/{id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(@PathVariable("id") long postId, HttpSession session) {
        Object uid = session.getAttribute(SessionKeys.UID);
        if (!(uid instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "not logged in");
        }
        postLikeService.like(postId, userId);
    }

    @DeleteMapping("/{id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(@PathVariable("id") long postId, HttpSession session) {
        Object uid = session.getAttribute(SessionKeys.UID);
        if (!(uid instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "not logged in");
        }
        postLikeService.unlike(postId, userId);
    }
}

