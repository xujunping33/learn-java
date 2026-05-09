package learn.java.bootsocial.service;

import java.time.Duration;

import learn.java.bootsocial.config.MinioProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * S3 兼容对象存储（MinIO）（W31 Day213）：上传与 presigned GET。
 */
@Service
@Profile({"dev", "docker"})
@ConditionalOnBean(S3Client.class)
public class StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final MinioProperties props;

    public StorageService(S3Client s3Client, S3Presigner s3Presigner, MinioProperties props) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.props = props;
    }

    public String getBucket() {
        return props.getBucket();
    }

    /** 上传对象（覆盖同名 key）。 */
    public void putObject(String objectKey, byte[] body, String contentType) {
        PutObjectRequest.Builder b = PutObjectRequest.builder()
                .bucket(props.getBucket())
                .key(objectKey);
        if (contentType != null && !contentType.isBlank()) {
            b.contentType(contentType);
        }
        s3Client.putObject(b.build(), RequestBody.fromBytes(body));
    }

    /** 下载对象字节（服务端直连 S3，不走 presign）。 */
    public byte[] getObjectBytes(String objectKey) {
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(props.getBucket())
                .key(objectKey)
                .build();
        return s3Client.getObjectAsBytes(get).asByteArray();
    }

    /** 删除对象（用于失败补偿/清理）。 */
    public void deleteObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(props.getBucket())
                .key(objectKey)
                .build());
    }

    /** 生成可浏览器 GET 的预签名 URL（bucket 可 private）。 */
    public String presignedGetUrl(String objectKey) {
        GetObjectRequest get =
                GetObjectRequest.builder().bucket(props.getBucket()).key(objectKey).build();
        return s3Presigner
                .presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofSeconds(props.getPresignGetTtlSeconds()))
                        .getObjectRequest(get)
                        .build())
                .url()
                .toExternalForm();
    }
}
