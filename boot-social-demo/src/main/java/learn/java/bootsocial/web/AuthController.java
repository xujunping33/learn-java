package learn.java.bootsocial.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import learn.java.bootsocial.auth.SessionKeys;
import learn.java.bootsocial.config.OpenApiConfiguration;
import learn.java.bootsocial.model.User;
import learn.java.bootsocial.service.UserService;
import learn.java.bootsocial.web.dto.LoginRequest;
import learn.java.bootsocial.web.dto.ApiResult;
import learn.java.bootsocial.web.dto.MeResponse;
import learn.java.bootsocial.web.dto.RegisterRequest;
import learn.java.bootsocial.web.exception.BizException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "注册、登录（写入 Session Cookie）、`/me`、`logout`")
@RestController
@RequestMapping("/api")
@Profile({"dev", "docker"})
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "注册",
            description = "成功后写入 Session Cookie（名见配置 `app.session.cookie-name`，默认 JSESSIONID）；同源 Swagger Try it out 会自动携带")
    @ApiResponse(responseCode = "200", description = "success")
    @ApiResponse(responseCode = "400", description = "校验失败，`ApiError.details`")
    @ApiResponse(responseCode = "409", description = "用户名已存在")
    @PostMapping(
            path = "/auth/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<MeResponse> register(@Valid @RequestBody RegisterRequest body, HttpSession session) {
        User created = userService.register(body.username(), body.password());
        session.setAttribute(SessionKeys.UID, created.getId());
        return ApiResult.ok(new MeResponse(created.getId(), created.getUsername()));
    }

    @Operation(
            summary = "登录",
            description = "成功后写入 Session Cookie（名见 `app.session.cookie-name`，默认 JSESSIONID）")
    @ApiResponse(responseCode = "200", description = "success")
    @ApiResponse(responseCode = "400", description = "校验失败")
    @ApiResponse(responseCode = "401", description = "用户名或密码错误")
    @PostMapping(
            path = "/auth/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<MeResponse> login(@Valid @RequestBody LoginRequest body, HttpSession session) {
        User u = userService.login(body.username(), body.password());
        if (u == null) {
            throw new BizException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "invalid username or password");
        }
        session.setAttribute(SessionKeys.UID, u.getId());
        return ApiResult.ok(new MeResponse(u.getId(), u.getUsername()));
    }

    @Operation(summary = "登出")
    @ApiResponse(responseCode = "204", description = "Session 作废")
    @SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)
    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @Operation(summary = "当前用户", description = "需有效 Session")
    @ApiResponse(responseCode = "200", description = "success")
    @ApiResponse(responseCode = "401", description = "未登录")
    @SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)
    @GetMapping(path = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<MeResponse> me(HttpSession session) {
        Object uid = session.getAttribute(SessionKeys.UID);
        if (!(uid instanceof Long id)) {
            throw new BizException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "not logged in");
        }
        User u = userService.findById(id);
        if (u == null) {
            session.invalidate();
            throw new BizException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "not logged in");
        }
        return ApiResult.ok(new MeResponse(u.getId(), u.getUsername()));
    }
}

