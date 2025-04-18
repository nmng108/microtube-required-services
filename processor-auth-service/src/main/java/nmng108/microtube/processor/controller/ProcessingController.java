package nmng108.microtube.processor.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.jms.Queue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import nmng108.microtube.processor.dto.base.BaseResponse;
import nmng108.microtube.processor.service.ProcessingService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${api.base-path}")
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProcessingController {
    JmsTemplate jmsTemplate;
    Queue videoProcessingRequestQueue;
    ProcessingService processingService;
//    MessageSource messageSource;
    //    String basePath;

    public ProcessingController(
            JmsTemplate jmsTemplate,
            @Qualifier("videoProcessingRequestQueue") Queue videoProcessingRequestQueue,
            ProcessingService processingService,
            @Value("${api.base-path}") String basePath
    ) {
        this.jmsTemplate = jmsTemplate;
        this.videoProcessingRequestQueue = videoProcessingRequestQueue;
        this.processingService = processingService;
//        this.basePath = basePath;
    }

    @Operation(summary = "Set avatar for an user", description = "resource: must be either \"users\" or \"channels\".")
    @PutMapping(value = "/users/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadUserAvatar(@PathVariable("id") @Min(0) Long id, @RequestPart("file") MultipartFile file) {
        String url = processingService.uploadAvatar("users", id, file);

        return ResponseEntity.ok(BaseResponse.succeeded(url));
    }

    @Operation(summary = "Set avatar for a channel", description = "resource: must be either \"users\" or \"channels\".")
    @PutMapping(value = "/channels/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadChannelAvatar(@PathVariable("id") @Min(0) Long id, @RequestPart("file") MultipartFile file) {
        String url = processingService.uploadAvatar("channels", id, file);

        return ResponseEntity.ok(BaseResponse.succeeded(url));
    }

    @PutMapping(value = "/videos/{id}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadThumbnail(@PathVariable("id") @Min(0) Long id, @RequestPart("file") MultipartFile file) {
        String url = processingService.uploadThumbnail(id, file);

        return ResponseEntity.ok(BaseResponse.succeeded(url));
    }

    // Cannot use PATCH, which change response's Content-Type of "/auth/details" to application/octet when called by other services.
    @PutMapping(value = "/videos/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadVideo(@PathVariable("id") @Min(0) Long id, @RequestPart("file") MultipartFile file) {
        processingService.uploadVideo(id, file);
        jmsTemplate.convertAndSend(videoProcessingRequestQueue, id);

        return ResponseEntity.accepted().body(BaseResponse.succeeded());
    }
}
