package learn.java.bootsocial.config;

import java.net.URI;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Profile({"dev", "docker"})
@ConditionalOnProperty(prefix = "app.minio", name = "enabled", havingValue = "true")
public class MinioS3Configuration {

    @Bean(destroyMethod = "close")
    S3Client s3Client(MinioProperties props) {
        validateCredentials(props);
        return S3Client.builder()
                .endpointOverride(URI.create(props.getEndpoint().trim()))
                .region(Region.of(props.getRegion().trim()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.getAccessKey().trim(), props.getSecretKey())))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(props.isPathStyleAccess())
                        .build())
                .build();
    }

    @Bean(destroyMethod = "close")
    S3Presigner s3Presigner(MinioProperties props) {
        validateCredentials(props);
        String ep = props.getPublicEndpoint();
        if (ep == null || ep.isBlank()) {
            ep = props.getEndpoint();
        }
        return S3Presigner.builder()
                .endpointOverride(URI.create(ep.trim()))
                .region(Region.of(props.getRegion().trim()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.getAccessKey().trim(), props.getSecretKey())))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(props.isPathStyleAccess())
                        .build())
                .build();
    }

    private static void validateCredentials(MinioProperties props) {
        if (props.getAccessKey() == null || props.getAccessKey().isBlank()) {
            throw new IllegalStateException("app.minio.access-key is required when app.minio.enabled=true");
        }
        if (props.getSecretKey() == null || props.getSecretKey().isBlank()) {
            throw new IllegalStateException("app.minio.secret-key is required when app.minio.enabled=true");
        }
        if (props.getBucket() == null || props.getBucket().isBlank()) {
            throw new IllegalStateException("app.minio.bucket is required when app.minio.enabled=true");
        }
    }
}
