package nmng108.microtube.processor.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import nmng108.microtube.processor.util.SimpleMessageInterpolator;
import nmng108.microtube.processor.util.constant.Constants;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionResponse extends BaseResponse<Object> {
    String errorCode;
    /**
     * The list of error details.<br>
     * - If the HTTP status is 4xx (client error), this list may be used to show to end user's UI.<br>
     * - If the HTTP status is 5xx (server error), this list should be used only to inform API users what's being wrong with the server.
     * <br>
     * There is only 1 format: {@link List}. Element inside the list can be either a string or a POJO.
     */
    Object details;
    /**
     * Provide identity and traceability to an exception response.
     */
    final String requestId;
    final String timestamp;

    /**
     * Do not provide any error code as well as error details. Should be used only for quick dev & test.
     */
    // level-1 constructor
    public ExceptionResponse() {
        super(CustomResponseStatus.FAILED);
        this.requestId = UUID.randomUUID().toString();
        this.timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
    }

    /**
     * Provide only error code but not any details.
     */
    // level-3 constructor
    public ExceptionResponse(ErrorCode errorCode) {
        this(errorCode, LocaleContextHolder.getLocale());
    }

    /**
     * Provide only error code but not any details.
     */
    // level-2 constructor
    public ExceptionResponse(ErrorCode errorCode, Locale locale) {
        this();
        this.errorCode = errorCode.code;
        this.message = SimpleMessageInterpolator.getMessage(Constants.MessageSources.ERROR_CODES, locale, errorCode.code);
    }

    /**
     * Preferred to be used for responding to a form submission.
     *
     * @param details the list of messages to be shown to end user's UI.
     */
    // level-4 constructor
    public ExceptionResponse(ErrorCode errorCode, List<String> details) {
        this(errorCode, details, LocaleContextHolder.getLocale());
    }

    /**
     * Preferred to be used for responding to user's actions (also including a form submission).
     *
     * @param details the list of messages to be shown to end user's UI.
     */
    // level-3 constructor
    public ExceptionResponse(ErrorCode errorCode, List<String> details, Locale locale) {
        this(errorCode, locale);
        this.details = details.stream()
                .map((detail) -> {
                    try {
                        return SimpleMessageInterpolator.getMessage(Constants.MessageSources.ERRORS, locale, detail);
                    } catch (MissingResourceException e) {
                        return detail;
                    }
                })
                .toList();
    }

    /**
     * Preferred to be used for responding to a form submission.
     *
     * @param details the keys can be field names if caught validation exceptions
     *                or of unsigned integer if the error is not specific to any param / input field (in a form).
     */
    // level-4 constructor
    public ExceptionResponse(ErrorCode errorCode, Map<?, String> details) {
        this(errorCode, details, LocaleContextHolder.getLocale());
    }

    /**
     * Preferred to be used for responding to a form submission.
     *
     * @param details the keys can be field names if caught validation exceptions
     *                or of unsigned integer (just like an {@link List}) if the error is not specific to any param / input field (in a form).
     */
    // level-3 constructor
    public ExceptionResponse(ErrorCode errorCode, Map<?, String> details, Locale locale) {
        this(errorCode, locale);
        this.details = details;

        details.entrySet().forEach((entry) -> {
            try {
                entry.setValue(SimpleMessageInterpolator.getMessage(Constants.MessageSources.ERRORS, locale, entry.getValue()));
            } catch (MissingResourceException ignored) {
                // If a message code was already defined by some dependencies, for example Bean Validation & Hibernate,
                // our customization (if any) should take effect inside their code (in this case is AbstractMessageInterpolator)
                // before running into this method, and thus this statement will fail and throw MissingResourceException.
            }
        });
    }

    /**
     * @param details can accept any value, so use this constructor with caution.
     */
    // level-2 constructor
    public ExceptionResponse(ErrorCode errorCode, @Nullable Object details) {
        this(errorCode, details, LocaleContextHolder.getLocale());
    }

    /**
     * @param details can accept any value, so use this constructor with caution.
     */
    // level-3 constructor
    public ExceptionResponse(ErrorCode errorCode, @Nullable Object details, Locale locale) {
        this(errorCode, locale);

        if (details != null) {
            Assert.isTrue(details instanceof List<?> || details instanceof Map<?, ?>, "details must be a list or a map");
        }

        this.details = details;
    }

//    /**
//     * This derived constructor is no longer used for this class. Hide the use of this from external classes.
//     */
//    private ExceptionResponse(CustomResponseStatus customResponseStatus) {}
//
//    /**
//     * This derived constructor is no longer used for this class. Hide the use of this from external classes.
//     */
//    private ExceptionResponse(Object data) {}
//
//    /**
//     * This derived constructor is no longer used for this class. Hide the use of this from external classes.
//     */
//    private ExceptionResponse(int status, String message, Object data) {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExceptionResponse that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(errorCode, that.errorCode) && Objects.equals(details, that.details) && Objects.equals(requestId, that.requestId) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), errorCode, details, requestId, timestamp);
    }

    @Override
    public String toString() {
        return "ExceptionResponse{" +
                "code='" + errorCode + '\'' +
                ", details=" + details +
                ", requestId='" + requestId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", message='" + message + '\'' +
                ", status=" + status +
                '}';
    }

    public record ViolatedField(String field, String message) {
    }
}
