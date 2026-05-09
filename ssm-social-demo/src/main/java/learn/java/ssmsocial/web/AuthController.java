package learn.java.ssmsocial.web;

import jakarta.servlet.http.HttpSession;
import learn.java.ssmsocial.auth.SessionKeys;
import learn.java.ssmsocial.model.User;
import learn.java.ssmsocial.service.UserService;
import learn.java.ssmsocial.web.dto.AuthRequest;
import learn.java.ssmsocial.web.dto.UserMeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(
            path = "/auth/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public UserMeResponse register(@RequestBody AuthRequest body, HttpSession session) {
        if (body == null) {
            throw new IllegalArgumentException("body is required");
        }
        User created = userService.register(body.username(), body.password());
        session.setAttribute(SessionKeys.UID, created.getId());
        return new UserMeResponse(created.getId(), created.getUsername());
    }

    @PostMapping(
            path = "/auth/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public UserMeResponse login(@RequestBody AuthRequest body, HttpSession session) {
        if (body == null) {
            throw new IllegalArgumentException("body is required");
        }
        User u = userService.login(body.username(), body.password());
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid username or password");
        }
        session.setAttribute(SessionKeys.UID, u.getId());
        return new UserMeResponse(u.getId(), u.getUsername());
    }

    @PostMapping(path = "/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @GetMapping(path = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserMeResponse me(HttpSession session) {
        Object uid = session.getAttribute(SessionKeys.UID);
        if (!(uid instanceof Long id)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "not logged in");
        }
        User u = userService.findById(id);
        if (u == null) {
            session.invalidate();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "not logged in");
        }
        return new UserMeResponse(u.getId(), u.getUsername());
    }
}

