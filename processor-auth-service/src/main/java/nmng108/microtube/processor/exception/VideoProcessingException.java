package nmng108.microtube.processor.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

import java.io.File;
import java.nio.file.Path;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoProcessingException extends RuntimeException {
    final long videoId;
    File localOriginalFile;
    Path localDestinationDirectory;

    public VideoProcessingException(long videoId, String message) {
        super(message);
        this.videoId = videoId;
    }

    public VideoProcessingException(long videoId, String message, Throwable cause) {
        super(message, cause);
        this.videoId = videoId;
    }

    public VideoProcessingException(long videoId, Throwable cause) {
        super(cause);
        this.videoId = videoId;
    }

    public VideoProcessingException(long videoId, @Nullable File localOriginalFile, @Nullable Path localDestinationDirectory, Throwable cause) {
        super(cause);
        this.videoId = videoId;
        this.localOriginalFile = localOriginalFile;
        this.localDestinationDirectory = localDestinationDirectory;
    }

    public VideoProcessingException(long videoId, @Nullable File localOriginalFile, @Nullable Path localDestinationDirectory, String message) {
        super(message);
        this.videoId = videoId;
        this.localOriginalFile = localOriginalFile;
        this.localDestinationDirectory = localDestinationDirectory;
    }
}
