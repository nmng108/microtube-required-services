package nmng108.microtube.processor.configuration;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioAsyncClient;
import io.minio.MinioClient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ObjectStoreConfiguration {
    String url;
    String username;
    String password;
    String hlsBucketName;
    String userStoreBucketName;

    public ObjectStoreConfiguration(
            @Value("${datasource.object-store.url}") String url,
            @Value("${datasource.object-store.username}") String username,
            @Value("${datasource.object-store.password}") String password,
            @Value("${datasource.object-store.bucket.hls}") String hlsBucketName,
            @Value("${datasource.object-store.bucket.user-store}") String userStoreBucketName
    ) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.hlsBucketName = hlsBucketName;
        this.userStoreBucketName = userStoreBucketName;
    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder().endpoint(url).credentials(username, password).build();
    }

    @Bean
    @SneakyThrows
    public MinioAsyncClient minioAsyncClient() {
        MinioAsyncClient minioAsyncClient = MinioAsyncClient.builder().endpoint(url).credentials(username, password).build();

        if (!minioAsyncClient.bucketExists(BucketExistsArgs.builder().bucket(hlsBucketName).build()).get()) {
            minioAsyncClient.makeBucket(MakeBucketArgs.builder().bucket(hlsBucketName).objectLock(true).build()).get();
        }

        if (!minioAsyncClient.bucketExists(BucketExistsArgs.builder().bucket(userStoreBucketName).build()).get()) {
            minioAsyncClient.makeBucket(MakeBucketArgs.builder().bucket(userStoreBucketName).objectLock(true).build()).get();
        }

        log.info(STR."Buckets '\{hlsBucketName}' and '\{userStoreBucketName}' had been created");

        return minioAsyncClient;
    }
}
