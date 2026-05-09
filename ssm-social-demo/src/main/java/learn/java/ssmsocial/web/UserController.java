package learn.java.ssmsocial.web;

import learn.java.ssmsocial.model.User;
import learn.java.ssmsocial.service.UserService;
import learn.java.ssmsocial.web.dto.CreateUserRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Day163：最小联通接口（保留给 Day163 smoke；Day164 新增 /api/auth/* 为主）。 */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public User create(@RequestBody CreateUserRequest body) {
        String username = body == null ? null : body.username();
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        // 用固定 demo 密码注册一条用户（避免与 Day164 注册接口互相影响）
        return userService.register(username.trim(), "demo123");
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public User getByUsername(@RequestParam("username") String username) {
        User u = userService.findByUsername(username);
        if (u == null) {
            throw new IllegalArgumentException("user not found");
        }
        return u;
    }
}

