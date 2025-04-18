package nmng108.microtube.processor.service;

import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

public interface ProcessingService {
    @Nullable
    String uploadAvatar(String resource, long id, MultipartFile file);

    @Nullable
    String uploadThumbnail(long id, MultipartFile file);

    void uploadVideo(long id, MultipartFile file);
}