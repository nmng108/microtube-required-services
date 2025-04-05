package nmng108.microtube.processor.controller;

import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import nmng108.microtube.processor.service.VideoProcessingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${api.base-path}/videos")
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VideoProcessingController {
//    String basePath;
    VideoProcessingService videoService;
//    MessageSource messageSource;

    public VideoProcessingController(@Value("${api.base-path}") String basePath, VideoProcessingService videoService) {
//        this.basePath = basePath;
        this.videoService = videoService;
    }

    @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@PathVariable("id") @Min(0) Long id, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.accepted().body(videoService.uploadVideo(id, file));
    }
}
