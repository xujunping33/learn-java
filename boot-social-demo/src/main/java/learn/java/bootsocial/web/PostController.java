package learn.java.bootsocial.web;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import learn.java.bootsocial.auth.SessionKeys;
import learn.java.bootsocial.cache.PostDetailCache;
import learn.java.bootsocial.config.AppProperties;
import learn.java.bootsocial.config.OpenApiConfiguration;
import learn.java.bootsocial.model.Comment;
import learn.java.bootsocial.model.Post;
import learn.java.bootsocial.service.CommentService;
import learn.java.bootsocial.service.PostDetailService;
import learn.java.bootsocial.service.PostLikeService;
import learn.java.bootsocial.service.PostService;
import learn.java.bootsocial.web.dto.ApiResult;
import learn.java.bootsocial.web.dto.CommentResponse;
import learn.java.bootsocial.web.dto.CreateCommentRequest;
import learn.java.bootsocial.web.dto.CreatePostRequest;
import learn.java.bootsocial.web.dto.PageResult;
import learn.java.bootsocial.web.dto.PostDetailResponse;
import learn.java.bootsocial.web.dto.PostResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@Tag(name = "Posts", description = "帖子列表/详情（GET 匿名）、发帖与评论点赞（写操作需 Session）")
@Validated
@RestController
@RequestMapping("/api/posts")
@Profile({"dev", "docker"})
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final PostLikeService postLikeService;
    private final AppProperties appProperties;
    private final PostDetailService postDetailService;

    public PostController(
            PostService postService,
            CommentService commentService,
            PostLikeService postLikeService,
            AppProperties appProperties,
            PostDetailService postDetailService) {
        this.postService = postService;
        this.commentService = commentService;
        this.postLikeService = postLikeService;
        this.appProperties = appProperties;
        this.postDetailService = postDetailService;
    }

    @Operation(
            summary = "发帖",
            security = {@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)},
            responses = {
                @ApiResponse(responseCode = "201", description = "创建成功，`ApiResult` 包 `PostResponse`"),
                @ApiResponse(responseCode = "400", description = "业务或校验错误"),
                @ApiResponse(responseCode = "401", description = "未登录")
            })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResult<PostResponse> create(
            @Valid @RequestBody CreatePostRequest body, @SessionAttribute(SessionKeys.UID) Long userId) {
        Post created = postService.createPost(userId, body.title(), body.content());
        return ApiResult.ok(toPostResponse(created));
    }

    @Operation(
            summary = "分页列表",
            description = "`data` 为 `PageResult`（items/total/page/size）；SQL 含 `LIMIT`",
            responses = {
                @ApiResponse(responseCode = "200"),
                @ApiResponse(responseCode = "400", description = "分页或 userId 参数非法（`VALIDATION_FAILED`）")
            })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<PageResult<PostResponse>> list(
            @Parameter(description = "页码，≥1，缺省为 1") @RequestParam(name = "page", required = false) @Min(1)
                    Integer page,
            @Parameter(
                            description = "每页条数 1～100；缺省为 app.api.default-page-size（默认 20）")
                    @RequestParam(name = "size", required = false)
                    @Min(1)
                    @Max(100)
                    Integer size,
            @Parameter(description = "标题模糊匹配，可选") @RequestParam(name = "keyword", required = false)
                    String keyword,
            @Parameter(description = "按作者过滤，≥1；可选") @RequestParam(name = "userId", required = false) @Min(1)
                    Long userId) {
        int p = page != null ? page : 1;
        int s = size != null ? size : appProperties.getApi().getDefaultPageSize();
        IPage<Post> pg = postService.pagePosts(p, s, keyword, userId);
        List<PostResponse> items = pg.getRecords().stream().map(PostController::toPostResponse).toList();
        return ApiResult.ok(new PageResult<>(items, pg.getTotal(), pg.getCurrent(), pg.getSize()));
    }

    @Operation(summary = "帖子下的评论列表", responses = {@ApiResponse(responseCode = "200")})
    @GetMapping(path = "/{id}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<List<CommentResponse>> listComments(
            @Parameter(description = "帖子 id") @PathVariable("id") long postId) {
        List<CommentResponse> list =
                commentService.listByPostId(postId).stream().map(PostController::toCommentResponse).toList();
        return ApiResult.ok(list);
    }

    @Operation(
            summary = "发表评论",
            security = {@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)},
            responses = {
                @ApiResponse(responseCode = "201"),
                @ApiResponse(responseCode = "400"),
                @ApiResponse(responseCode = "401", description = "未登录"),
                @ApiResponse(responseCode = "404", description = "帖子不存在")
            })
    @PostMapping(
            path = "/{id}/comments",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResult<CommentResponse> addComment(
            @Parameter(description = "帖子 id") @PathVariable("id") long postId,
            @Valid @RequestBody CreateCommentRequest body,
            @SessionAttribute(SessionKeys.UID) Long userId) {
        Comment c = commentService.addComment(postId, userId, body.content());
        return ApiResult.ok(toCommentResponse(c));
    }

    @Operation(
            summary = "点赞（幂等）",
            security = {@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)},
            responses = {@ApiResponse(responseCode = "204"), @ApiResponse(responseCode = "401")})
    @PostMapping("/{id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(
            @Parameter(description = "帖子 id") @PathVariable("id") long postId,
            @SessionAttribute(SessionKeys.UID) Long userId) {
        postLikeService.like(postId, userId);
    }

    @Operation(
            summary = "取消点赞",
            security = {@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)},
            responses = {@ApiResponse(responseCode = "204"), @ApiResponse(responseCode = "401")})
    @DeleteMapping("/{id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(
            @Parameter(description = "帖子 id") @PathVariable("id") long postId,
            @SessionAttribute(SessionKeys.UID) Long userId) {
        postLikeService.unlike(postId, userId);
    }

    /** 帖子详情：post + comments + likeCount（Day174 聚合） */
    @Operation(
            summary = "帖子详情",
            description =
                    "`PostDetailResponse`：嵌套 comments 与 likeCount。**不存在**时在 Redis 写短 TTL 占位 `"
                        + PostDetailCache.NULL_SENTINEL
                        + "`（穿透保护）；命中占位则直接 404 不打库。配置：`app.api.post-detail-absent-cache-ttl-seconds`。",
            responses = {@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<PostDetailResponse> detail(@Parameter(description = "帖子 id") @PathVariable("id") long id) {
        return ApiResult.ok(postDetailService.get(id));
    }

    private static PostResponse toPostResponse(Post p) {
        long likeCount = longOrZero(p.getLikeCount());
        return new PostResponse(
                longOrZero(p.getId()),
                longOrZero(p.getUserId()),
                p.getAuthorUsername(),
                likeCount,
                p.getTitle(),
                p.getContent(),
                p.getCreatedAt());
    }

    private static CommentResponse toCommentResponse(Comment c) {
        return new CommentResponse(
                longOrZero(c.getId()),
                longOrZero(c.getPostId()),
                longOrZero(c.getUserId()),
                c.getAuthorUsername(),
                c.getContent(),
                c.getCreatedAt());
    }

    /** JDK 21：<code>switch</code> + <code>null</code> / 类型 pattern，统一 Long → long */
    private static long longOrZero(Long v) {
        return switch (v) {
            case null -> 0L;
            case Long n -> n;
        };
    }
}
