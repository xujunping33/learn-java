package learn.java.bootsocial.config;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

    @Valid
    private final Api api = new Api();

    private final Cors cors = new Cors();

    @Valid
    private final Session session = new Session();

    public Api getApi() {
        return api;
    }

    public Cors getCors() {
        return cors;
    }

    public Session getSession() {
        return session;
    }

    public static class Api {
        /** 未传 `size` 时，`GET /api/posts` 的默认每页条数 */
        @Min(1)
        @Max(100)
        private int defaultPageSize = 20;

        /** 帖子详情缓存 TTL（秒），用于 `GET /api/posts/{id}`（W27 Day185） */
        @Min(1)
        @Max(3600)
        private int postDetailCacheTtlSeconds = 120;

        public int getDefaultPageSize() {
            return defaultPageSize;
        }

        public void setDefaultPageSize(int defaultPageSize) {
            this.defaultPageSize = defaultPageSize;
        }

        public int getPostDetailCacheTtlSeconds() {
            return postDetailCacheTtlSeconds;
        }

        public void setPostDetailCacheTtlSeconds(int postDetailCacheTtlSeconds) {
            this.postDetailCacheTtlSeconds = postDetailCacheTtlSeconds;
        }
    }

    public static class Cors {
        /** 非空时注册 `addMapping("/api/**")` 的全局 CORS；与 `allowCredentials` 配合须显式列 origin，勿用 * */
        private List<String> origins = new ArrayList<>();

        public List<String> getOrigins() {
            return origins;
        }

        public void setOrigins(List<String> origins) {
            this.origins = origins;
        }
    }

    public static class Session {
        @NotBlank
        private String cookieName = "JSESSIONID";

        public String getCookieName() {
            return cookieName;
        }

        public void setCookieName(String cookieName) {
            this.cookieName = cookieName;
        }
    }
}
