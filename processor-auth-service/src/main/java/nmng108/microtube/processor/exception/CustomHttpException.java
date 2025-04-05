package nmng108.microtube.processor.exception;

import lombok.Getter;
import nmng108.microtube.processor.dto.base.ErrorCode;
import nmng108.microtube.processor.dto.base.ExceptionResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Getter
public class CustomHttpException extends HttpStatusCodeException {
    private static final ErrorCode DEFAULT_ERROR_CODE = ErrorCode.E20001;

    protected final ErrorCode errorCode;
    /**
     * Corresponds to {@link ExceptionResponse#getDetails()}
     */
    protected final Object details;

//    public AbstractCustomHttpException(HttpStatus httpStatus) {
//        this(httpStatus, DEFAULT_ERROR_CODE);
//    }
//
//    public AbstractCustomHttpException(HttpStatus httpStatus, String message) {
//        this(httpStatus, DEFAULT_ERROR_CODE, message);
//    }
//
//    public AbstractCustomHttpException(HttpStatus httpStatus, ErrorCode errorCode) {
//        this(httpStatus, errorCode, (List<String>) null);
//    }
//
    public CustomHttpException(HttpStatus httpStatus, ErrorCode errorCode, String message) {
        this(httpStatus, errorCode, Collections.singletonList(message));
    }

    public CustomHttpException(HttpStatus httpStatus, ErrorCode errorCode, @Nullable List<String> details) {
        this(httpStatus, errorCode, details, null);
    }
//
//    /**
//     * Constructor with only errorCode and message.
//     */
//    public AbstractCustomHttpException(HttpStatus httpStatus, ErrorCode errorCode, @Nullable Map<Object, String> details) {
//        this(httpStatus, errorCode, details, null);
//    }
//
//    /**
//     * Constructor with errorCode and body.
//     */
//    public AbstractHttpException(ErrorCode errorCode, byte[] body, @Nullable Charset charset) {
//        this(errorCode, null, body, charset);
//    }
//
//    /**
//     * Constructor with errorCode, headers and body. By default, message will be set by concatenating {@link HttpStatus#value()} and {@link HttpStatus#getReasonPhrase()}.
//     */
//    public AbstractHttpException(ErrorCode errorCode, @Nullable HttpHeaders headers, @Nullable byte[] body, @Nullable Charset charset) {
//        this(errorCode, (List<String>) null, headers, body, charset);
//    }
//
//    /**
//     * Full-parameter constructor with headers and body.
//     */
//    public AbstractHttpException(ErrorCode errorCode, String message, @Nullable HttpHeaders headers, @Nullable byte[] body, @Nullable Charset charset) {
//        this(errorCode, Collections.singletonList(message), headers, body, charset);
//    }

    /**
     * Full-parameter constructor with headers and body.
     *
     * @param details {@link List}-structured.
     */
    public CustomHttpException(HttpStatus httpStatus, ErrorCode errorCode, @Nullable List<?> details, @Nullable HttpHeaders headers) {
        super("", httpStatus, errorCode.code, headers, null, null);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * Full-parameter constructor with headers and body.
     *
     * @param details {@link Map}-structured.
     */
    public CustomHttpException(HttpStatus httpStatus, ErrorCode errorCode, @Nullable Map<Object, String> details, @Nullable HttpHeaders headers) {
        super("", httpStatus, errorCode.code, headers, null, null);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ResponseEntity<ExceptionResponse> toResponse() {
        return ResponseEntity
                .status(this.getStatusCode())
                .headers(this.getResponseHeaders())
                .body(new ExceptionResponse(this.errorCode, details));
    }

    public ResponseEntity<ExceptionResponse> toResponse(Locale locale) {
        return ResponseEntity
                .status(this.getStatusCode())
                .headers(this.getResponseHeaders())
                .body(new ExceptionResponse(this.errorCode, details, locale));
    }
}
