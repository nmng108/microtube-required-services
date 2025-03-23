package nmng108.microtube.processor.service;

import nmng108.microtube.processor.dto.base.BaseResponse;
import org.springframework.web.multipart.MultipartFile;

public interface VideoService {
    BaseResponse<Void> uploadVideo(long id, MultipartFile file);
}