package nmng108.microtube.processor.configuration;

import io.minio.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;


@Configuration
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ObjectStoreConfiguration {
    String url;
    String username;
    String password;
    String hlsBucketName;
    String temporaryBucketName;
    String avatarBucketName;
    String thumbnailBucketName;
    String minioDownloadPolicyFilepath = "minio-download-policy.json";

    public ObjectStoreConfiguration(
            @Value("${datasource.object-store.url}") String url,
            @Value("${datasource.object-store.username}") String username,
            @Value("${datasource.object-store.password}") String password,
            @Value("${datasource.object-store.bucket.hls}") String hlsBucketName,
            @Value("${datasource.object-store.bucket.temporary}") String temporaryBucketName,
            @Value("${datasource.object-store.bucket.avatar}") String avatarBucketName,
            @Value("${datasource.object-store.bucket.thumbnail}") String thumbnailBucketName
    ) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.hlsBucketName = hlsBucketName;
        this.temporaryBucketName = temporaryBucketName;
        this.avatarBucketName = avatarBucketName;
        this.thumbnailBucketName = thumbnailBucketName;
    }

    /**
     * Beside creating MinIO client bean, this method also creates buckets & sets allow-to-download policy for each bucket.
     */
    @Bean
    @SneakyThrows
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder().endpoint(url).credentials(username, password).build();
        String jsonDownloadPolicy = new String(
                FileCopyUtils.copyToByteArray(new ClassPathResource(minioDownloadPolicyFilepath).getInputStream()),
                StandardCharsets.UTF_8
        );

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(temporaryBucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(temporaryBucketName).objectLock(true).build());
        }

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(hlsBucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(hlsBucketName).objectLock(true).build());
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(hlsBucketName)
                    .config(jsonDownloadPolicy
                            .replaceAll("\\$\\{bucket}", hlsBucketName)
                            .replaceAll("\\$\\{prefix}", "*")
                    )
                    .build());
        }

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(avatarBucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(avatarBucketName).objectLock(true).build());
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(avatarBucketName)
                    .config(jsonDownloadPolicy
                            .replaceAll("\\$\\{bucket}", avatarBucketName)
                            .replaceAll("\\$\\{prefix}", "*")
                    )
                    .build());
        }

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(thumbnailBucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(thumbnailBucketName).objectLock(true).build());
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(thumbnailBucketName)
                    .config(jsonDownloadPolicy
                            .replaceAll("\\$\\{bucket}", thumbnailBucketName)
                            .replaceAll("\\$\\{prefix}", "*")
                    )
                    .build());
        }

        log.info(STR."Buckets '\{temporaryBucketName}', '\{hlsBucketName}', '\{avatarBucketName} and ''\{thumbnailBucketName}' had been created");

        return minioClient;
    }

    @Bean
    public MinioAsyncClient minioAsyncClient() {
        return MinioAsyncClient.builder().endpoint(url).credentials(username, password).build();
    }
}
