package nmng108.microtube.processor.service;

import io.minio.GetObjectResponse;
import io.minio.ObjectWriteResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface ObjectStoreService {
    void putObject(String bucketName, String objectName, MultipartFile file);
    void putObject(String bucketName, String objectName, File file, String contentType);
    CompletableFuture<ObjectWriteResponse> putObjectAsync(String bucketName, String objectName, MultipartFile file);
    CompletableFuture<ObjectWriteResponse> putObjectAsync(String bucketName, String objectName, File file, String contentType);
    GetObjectResponse getObject(String bucketName, String objectName);
    void removeObject(String bucketName, String objectName);
    CompletableFuture<Void> removeObjectAsync(String bucketName, String objectName);
}