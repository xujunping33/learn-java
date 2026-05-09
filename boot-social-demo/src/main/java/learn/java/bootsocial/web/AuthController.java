package learn.java.bootsocial.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.annotation.SaCheckLogin;
import learn.java.bootsocial.config.OpenApiConfiguration;
import learn.java.bootsocial.model.User;
import learn.java.bootsocial.config.AppProperties;
import learn.java.bootsocial.service.UserService;
import learn.java.bootsocial.web.dto.LoginRequest;
import learn.java.bootsocial.web.dto.ApiResult;
import learn.java.bootsocial.web.dto.AuthTokenResponse;
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

@Tag(name = "Auth", description = "注册、登录（Token）、`/me`、`logout`")
@RestController
@RequestMapping("/api")
@Profile({"dev", "docker"})
public class AuthController {

    private final UserService userService;
    private final AppProperties appProperties;

    public AuthController(UserService userService, AppProperties appProperties) {
        this.userService = userService;
        this.appProperties = appProperties;
    }

    @Operation(
            summary = "注册",
            description = "成功后返回 token（header: Authorization: Bearer <token>）")
    @ApiResponse(responseCode = "200", description = "success")
    @ApiResponse(responseCode = "400", description = "校验失败，`ApiError.details`")
    @ApiResponse(responseCode = "409", description = "用户名已存在")
    @PostMapping(
            path = "/auth/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<AuthTokenResponse> register(@Valid @RequestBody RegisterRequest body) {
        User created = userService.register(body.username(), body.password());
        loginByPolicy(created.getId());
        return ApiResult.ok(
                new AuthTokenResponse(
                        created.getId(),
                        created.getUsername(),
                        StpUtil.getTokenName(),
                        StpUtil.getTokenValue(),
                        "Bearer"));
    }

    @Operation(
            summary = "登录",
            description = "成功后返回 token（header: Authorization: Bearer <token>）")
    @ApiResponse(responseCode = "200", description = "success")
    @ApiResponse(responseCode = "400", description = "校验失败")
    @ApiResponse(responseCode = "401", description = "用户名或密码错误")
    @PostMapping(
            path = "/auth/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<AuthTokenResponse> login(@Valid @RequestBody LoginRequest body) {
        User u = userService.login(body.username(), body.password());
        if (u == null) {
            throw new BizException(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "invalid username or password");
        }
        loginByPolicy(u.getId());
        return ApiResult.ok(
                new AuthTokenResponse(u.getId(), u.getUsername(), StpUtil.getTokenName(), StpUtil.getTokenValue(), "Bearer"));
    }

    @Operation(summary = "登出")
    @ApiResponse(responseCode = "204", description = "Session 作废")
    @SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)
    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SaCheckLogin
    public void logout() {
        StpUtil.logout();
    }

    @Operation(summary = "当前用户", description = "需有效 Session")
    @ApiResponse(responseCode = "200", description = "success")
    @ApiResponse(responseCode = "401", description = "未登录")
    @SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)
    @GetMapping(path = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @SaCheckLogin
    public ApiResult<MeResponse> me() {
        long id = StpUtil.getLoginIdAsLong();
        User u = userService.findById(id);
        if (u == null) {
            StpUtil.logout();
            throw new BizException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "not logged in");
        }
        return ApiResult.ok(new MeResponse(u.getId(), u.getUsername()));
    }

    private void loginByPolicy(long userId) {
        AppProperties.MultiLoginPolicy policy = appProperties.getAuth().getMultiLoginPolicy();
        if (policy == null) {
            policy = AppProperties.MultiLoginPolicy.ALLOW;
        }
        switch (policy) {
            case ALLOW -> StpUtil.login(userId);
            case REPLACE -> {
                // 互踢：先踢掉该账号所有已登录会话，再重新登录
                StpUtil.kickout(userId);
                StpUtil.login(userId);
            }
            case DENY -> {
                if (StpUtil.getStpLogic().isLogin(userId)) {
                    throw new BizException(HttpStatus.CONFLICT, "ALREADY_LOGGED_IN", "user already logged in");
                }
                StpUtil.login(userId);
            }
        }
    }
}

