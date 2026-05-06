package learn.java.bootsocial.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuthInterceptorTest {

    @Test
    void requiresAuth_matchesWritePathsOnly() {
        assertThat(AuthInterceptor.requiresAuth("GET", "/api/posts")).isFalse();
        assertThat(AuthInterceptor.requiresAuth("GET", "/api/posts/1")).isFalse();
        assertThat(AuthInterceptor.requiresAuth("GET", "/api/posts/1/comments")).isFalse();

        assertThat(AuthInterceptor.requiresAuth("POST", "/api/posts")).isTrue();
        assertThat(AuthInterceptor.requiresAuth("POST", "/api/posts/9/comments")).isTrue();
        assertThat(AuthInterceptor.requiresAuth("POST", "/api/posts/9/like")).isTrue();
        assertThat(AuthInterceptor.requiresAuth("DELETE", "/api/posts/9/like")).isTrue();

        assertThat(AuthInterceptor.requiresAuth("POST", "/api/posts/abc/comments")).isFalse();
    }
}
