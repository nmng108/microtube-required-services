package nmng108.microtube.processor.exception;

import lombok.Getter;

@Getter
public class VideoProcessingException extends RuntimeException {
    private long videoId;

    public VideoProcessingException(long videoId) {
        this.videoId = videoId;
    }

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

    public VideoProcessingException(long videoId, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.videoId = videoId;
    }
}
