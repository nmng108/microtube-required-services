package nmng108.microtube.processor.service;

import io.minio.GetObjectResponse;
import io.minio.ObjectWriteResponse;
import io.minio.Result;
import io.minio.messages.DeleteError;
import io.minio.messages.Item;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface ObjectStoreService {
    Iterable<Result<Item>> listObjectsRecursive(String bucketName, String prefix);

    GetObjectResponse getObject(String bucketName, String objectName);

    void putObject(String bucketName, String objectName, MultipartFile file);

    CompletableFuture<ObjectWriteResponse> putObjectAsync(String bucketName, String objectName, MultipartFile file);

    void putObject(String bucketName, String objectName, File file, String contentType);

    CompletableFuture<ObjectWriteResponse> putObjectAsync(String bucketName, String objectName, File file, String contentType);

    void removeObject(String bucketName, String objectName);

    CompletableFuture<Void> removeObjectAsync(String bucketName, String objectName);

    Iterable<Result<DeleteError>> removeObjectsWithPrefix(String bucketName, String prefix);

    @Nullable
    String getDownloadUrl(@Nullable String objectPath);
}