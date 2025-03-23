package nmng108.microtube.processor.exception;

import lombok.Getter;
import nmng108.microtube.processor.dto.base.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class BadRequestException extends CustomHttpException {
    private static final HttpStatus DEFAULT_HTTP_STATUS = HttpStatus.BAD_REQUEST;
    private static final ErrorCode DEFAULT_ERROR_CODE = ErrorCode.E00003;

    /**
     * Apply the default error code: {@link ErrorCode#E00003}.
     */
    public BadRequestException() {
        this(DEFAULT_ERROR_CODE);
    }

    /**
     * Apply the default error code: {@link ErrorCode#E00003}.
     */
    public BadRequestException(String message) {
        this(DEFAULT_ERROR_CODE, message);
    }

    public BadRequestException(ErrorCode errorCode) {
        this(errorCode, (List<String>) null);
    }

    public BadRequestException(ErrorCode errorCode, String message) {
        this(errorCode, Collections.singletonList(message));
    }

    public BadRequestException(ErrorCode errorCode, @Nullable List<?> details) {
        this(errorCode, details, null);
    }

    /**
     * Constructor with only errorCode and message.
     */
    public BadRequestException(ErrorCode errorCode, @Nullable Map<Object, String> details) {
        this(errorCode, details, null);
    }

    /**
     * Full-parameter constructor with headers and body.
     *
     * @param details {@link List}-structured.
     */
    public BadRequestException(ErrorCode errorCode, @Nullable List<?> details, @Nullable HttpHeaders headers) {
        super(DEFAULT_HTTP_STATUS, errorCode, details, headers);
    }

    /**
     * Full-parameter constructor with headers and body.
     *
     * @param details {@link Map}-structured.
     */
    public BadRequestException(ErrorCode errorCode, @Nullable Map<Object, String> details, @Nullable HttpHeaders headers) {
        super(DEFAULT_HTTP_STATUS, errorCode, details, headers);
    }
}
