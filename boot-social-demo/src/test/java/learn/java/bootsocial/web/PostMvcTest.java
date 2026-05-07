package learn.java.bootsocial.web;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import learn.java.bootsocial.cache.PostDetailCache;
import learn.java.bootsocial.cache.PostDetailCache.Peek;
import learn.java.bootsocial.auth.SessionKeys;
import learn.java.bootsocial.config.AppProperties;
import learn.java.bootsocial.config.AuthInterceptor;
import learn.java.bootsocial.config.DevWebConfig;
import learn.java.bootsocial.model.Post;
import learn.java.bootsocial.service.CommentService;
import learn.java.bootsocial.service.PostLikeService;
import learn.java.bootsocial.service.PostService;
import learn.java.bootsocial.web.dto.PostDetailResponse;
import learn.java.bootsocial.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PostController.class)
@ActiveProfiles("dev")
@TestPropertySource(properties = "app.api.default-page-size=11")
@EnableConfigurationProperties(AppProperties.class)
@Import({DevWebConfig.class, AuthInterceptor.class, GlobalExceptionHandler.class})
class PostMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private PostLikeService postLikeService;

    @MockitoBean
    private PostDetailCache postDetailCache;

    @Test
    void createPostWithoutSessionReturns401() throws Exception {
        mockMvc.perform(
                        post("/api/posts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"hello\",\"content\":\"world\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void createPostWithBlankTitleReturns400() throws Exception {
        mockMvc.perform(
                        post("/api/posts")
                                .sessionAttr(SessionKeys.UID, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\" \",\"content\":\"world\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void listPostsWithInvalidPageReturns400() throws Exception {
        mockMvc.perform(get("/api/posts").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void listPostsWithOversizedPageSizeReturns400() throws Exception {
        mockMvc.perform(get("/api/posts").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void listPostsWithInvalidUserIdReturns400() throws Exception {
        mockMvc.perform(get("/api/posts").param("userId", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void listPostsWithoutSizeUsesConfiguredDefaultPageSize() throws Exception {
        Page<Post> empty = new Page<>(1, 11);
        empty.setRecords(Collections.emptyList());
        empty.setTotal(0);
        when(postService.pagePosts(1, 11, null, null)).thenReturn(empty);

        mockMvc.perform(get("/api/posts").param("page", "1")).andExpect(status().isOk());

        verify(postService).pagePosts(1, 11, null, null);
    }

    @Test
    void listPostsExplicitSizeOverridesDefault() throws Exception {
        Page<Post> empty = new Page<>(1, 5);
        empty.setRecords(Collections.emptyList());
        empty.setTotal(0);
        when(postService.pagePosts(1, 5, null, null)).thenReturn(empty);

        mockMvc.perform(get("/api/posts").param("page", "1").param("size", "5"))
                .andExpect(status().isOk());

        verify(postService).pagePosts(1, 5, null, null);
    }

    @Test
    void detailWhenNegativeCacheAbsent_returns404WithoutDbRoundTrip() throws Exception {
        when(postDetailCache.peek(42L)).thenReturn(Peek.ABSENT);

        mockMvc.perform(get("/api/posts/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        verify(postService, never()).getPostDetail(anyLong());
        verify(commentService, never()).listByPostId(anyLong());
    }

    @Test
    void detailWhenCachedHit_returnsOkWithoutDbRoundTrip() throws Exception {
        PostDetailResponse body =
                new PostDetailResponse(
                        1L,
                        2L,
                        "u",
                        0L,
                        "titled",
                        "body",
                        "2026-05-07T08:00:00",
                        Collections.emptyList());
        when(postDetailCache.peek(1L)).thenReturn(new Peek.Hit(body));

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("titled"));

        verify(postService, never()).getPostDetail(anyLong());
        verify(commentService, never()).listByPostId(anyLong());
    }
}
