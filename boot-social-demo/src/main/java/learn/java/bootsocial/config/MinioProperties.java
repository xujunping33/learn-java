package learn.java.bootsocial.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * MinIO（S3 兼容）客户端配置（W31 Day213）。
 * <p>
 * 启用条件：{@code app.minio.enabled=true}，且 dev/docker profile；Docker 中通过环境变量注入 endpoint/密钥。
 */
@ConfigurationProperties(prefix = "app.minio")
@Validated
public class MinioProperties {

    private boolean enabled = false;

    /** 例如 {@code http://minio:9000} 或 {@code http://127.0.0.1:9000} */
    private String endpoint = "http://127.0.0.1:9000";

    /**
     * 对外可访问的 endpoint（用于生成 presigned URL 的 host）。
     * <p>
     * 典型：Docker Compose 内部访问用 {@link #endpoint} = {@code http://minio:9000}，
     * 但浏览器/宿主机访问应使用 {@code http://127.0.0.1:9000} 或公网域名。
     * <p>
     * 留空则沿用 {@link #endpoint}。
     */
    private String publicEndpoint = "";

    /** MinIO 不校验区域时仍建议显式填一个（SDK 需要 region） */
    private String region = "us-east-1";

    private String accessKey = "";
    private String secretKey = "";
    private String bucket = "boot-social";

    /** MinIO 通常需要 path-style */
    private boolean pathStyleAccess = true;

    /** presigned GET 有效期（秒） */
    @Min(60)
    @Max(604800)
    private int presignGetTtlSeconds = 300;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public String getPublicEndpoint() {
        return publicEndpoint;
    }

    public void setPublicEndpoint(String publicEndpoint) {
        this.publicEndpoint = publicEndpoint;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public boolean isPathStyleAccess() {
        return pathStyleAccess;
    }

    public void setPathStyleAccess(boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
    }

    public int getPresignGetTtlSeconds() {
        return presignGetTtlSeconds;
    }

    public void setPresignGetTtlSeconds(int presignGetTtlSeconds) {
        this.presignGetTtlSeconds = presignGetTtlSeconds;
    }
}
