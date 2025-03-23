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
public class ForbiddenException extends CustomHttpException {
    private static final HttpStatus DEFAULT_HTTP_STATUS = HttpStatus.FORBIDDEN;
    private static final ErrorCode DEFAULT_ERROR_CODE = ErrorCode.E00002; // change this

    /**
     * Apply the default error code: {@link ErrorCode#E00002}.
     */
    public ForbiddenException() {
        this(DEFAULT_ERROR_CODE);
    }

    /**
     * Apply the default error code: {@link ErrorCode#E00002}.
     */
    public ForbiddenException(String message) {
        this(DEFAULT_ERROR_CODE, message);
    }

    public ForbiddenException(ErrorCode errorCode) {
        this(errorCode, (List<String>) null);
    }

    public ForbiddenException(ErrorCode errorCode, String message) {
        this(errorCode, Collections.singletonList(message));
    }

    public ForbiddenException(ErrorCode errorCode, @Nullable List<String> details) {
        this(errorCode, details, null);
    }

    /**
     * Constructor with only errorCode and message.
     */
    public ForbiddenException(ErrorCode errorCode, @Nullable Map<Object, String> details) {
        this(errorCode, details, null);
    }

    /**
     * Full-parameter constructor with headers and body.
     *
     * @param details {@link List}-structured.
     */
    public ForbiddenException(ErrorCode errorCode, @Nullable List<String> details, @Nullable HttpHeaders headers) {
        super(DEFAULT_HTTP_STATUS, errorCode, details, headers);
    }

    /**
     * Full-parameter constructor with headers and body.
     *
     * @param details {@link Map}-structured.
     */
    public ForbiddenException(ErrorCode errorCode, @Nullable Map<Object, String> details, @Nullable HttpHeaders headers) {
        super(DEFAULT_HTTP_STATUS, errorCode, details, headers);
    }
}
