package nmng108.microtube.processor.service.impl;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import nmng108.microtube.processor.configuration.ObjectStoreConfiguration;
import nmng108.microtube.processor.service.ObjectStoreService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MinioObjectStoreServiceImpl implements ObjectStoreService {
    ObjectStoreConfiguration configuration;
    MinioClient minioClient;
    MinioAsyncClient minioAsyncClient;

    public MinioObjectStoreServiceImpl(
            ObjectStoreConfiguration configuration,
            @Qualifier("minioClient") MinioClient minioClient,
            @Qualifier("minioAsyncClient") MinioAsyncClient minioAsyncClient
    ) {
        this.configuration = configuration;
        this.minioClient = minioClient;
        this.minioAsyncClient = minioAsyncClient;
    }

    @Override
    public Iterable<Result<Item>> listObjectsRecursive(String bucketName, String prefix) {
        return minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).recursive(true).build());
    }

    @Override
    public GetObjectResponse getObject(String bucketName, String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void putObject(String bucketName, String objectName, MultipartFile file) {
        try (InputStream inputStream = new BufferedInputStream(file.getInputStream(), 64 * 1024)) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .contentType(file.getContentType())
                    .stream(inputStream, file.getSize(), -1)
                    .build());
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<ObjectWriteResponse> putObjectAsync(String bucketName, String objectName, MultipartFile file) {
        try (InputStream inputStream = new BufferedInputStream(file.getInputStream(), 64 * 1024)) {
            return minioAsyncClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .contentType(file.getContentType())
                    .stream(inputStream, file.getSize(), -1)
                    .build());
        } catch (InsufficientDataException | IOException | NoSuchAlgorithmException | InvalidKeyException |
                 XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void putObject(String bucketName, String objectName, File file, String contentType) {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            long size = Files.readAttributes(file.toPath(), BasicFileAttributes.class).size();

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .contentType(contentType)
                    .stream(inputStream, size, -1)
                    .build());
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<ObjectWriteResponse> putObjectAsync(String bucketName, String objectName, File file, String contentType) {
        try {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            long size = Files.readAttributes(file.toPath(), BasicFileAttributes.class).size();

            return minioAsyncClient.putObject(PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .contentType(contentType)
                            .stream(inputStream, size, -1)
                            .build())
                    .thenApply((response) -> {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        return response;
                    })
                    .exceptionally((throwable) -> {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        return null;
                    });
        } catch (InsufficientDataException | IOException | NoSuchAlgorithmException | InvalidKeyException |
                 XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeObject(String bucketName, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<Void> removeObjectAsync(String bucketName, String objectName) {
        try {
            return minioAsyncClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (InsufficientDataException | IOException | NoSuchAlgorithmException | InvalidKeyException |
                 XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<Result<DeleteError>> removeObjectsWithPrefix(String bucketName, String prefix) {
        List<DeleteObject> deleteObjects = new LinkedList<>();

        listObjectsRecursive(bucketName, prefix).forEach((o) -> {
            try {
                var item = o.get();

                deleteObjects.add(new DeleteObject(item.objectName()));
            } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                     InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                     XmlParserException e) {
                throw new RuntimeException(e);
            }
        });

        return minioAsyncClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(deleteObjects).build());
    }

    @Override
    @Nullable
    public String getDownloadUrl(@Nullable String objectPath) {
        if (!StringUtils.hasText(objectPath)) {
            return null;
        }

        try {
//            String[] splitPath = objectPath.split(":");

            return STR."\{configuration.getUrl()}/\{objectPath.replace(":", "/")}";
//        return STR."\{configuration.getUrl()}/api/v1/buckets/\{splitPath[0]}/objects/download?prefix=\{URLEncoder.encode(splitPath[1], StandardCharsets.UTF_8)}";
        } catch (Exception e) {
            return null;
        }
    }
}
