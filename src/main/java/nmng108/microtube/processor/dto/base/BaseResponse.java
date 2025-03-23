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

import java.util.Locale;
import java.util.Objects;

@Getter
@FieldDefaults(level = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {
//    @JsonIgnore
//    CustomResponseStatus customResponseStatus;
    /**
     * Provides coarse-grained application-specific status code to API users.
     */
    int status; // succeeded: 0; failed: -1
    /**
     * This attribute is prone to provide a human-readable short message corresponding to the status code to API users.
     * Therefore, this should not be rendered to end user's UI.
     * <br>
     * Source of message: from either of {@link CustomResponseStatus#messageCode or {@link ErrorCode#code}}.
     */
    String message;
    T data;

    /**
     * Allows to construct no-data body with {@link CustomResponseStatus#SUCCESS} selected by default.
     */
    public BaseResponse() {
        this(CustomResponseStatus.SUCCESS);
    }

    /**
     * Allows to construct no-data body with chosen status.
     */
    public BaseResponse(CustomResponseStatus customResponseStatus) {
        this(customResponseStatus, null, null, null);
    }

    /**
     * Set {@link CustomResponseStatus#SUCCESS} by default.
     * You can use the {@link BaseResponse#succeeded(T)} factory methods instead.
     */
    public BaseResponse(T data) {
        this(CustomResponseStatus.SUCCESS, null, null, data);
    }

    /**
     * The most flexible constructor bound to {@link CustomResponseStatus}.
     * This shouldn't be used directly anywhere else.
     *
     * @param customResponseStatus must be either of {@link CustomResponseStatus#SUCCESS_WITH_INFORMATION}
     *                             or {@link CustomResponseStatus#SUCCESS_WITH_WARNING}.
     * @param locale               set null to get Locale from {@link LocaleContextHolder#getLocale()}.
     * @param messageCode          can be additional information or a warning.
     */
    protected BaseResponse(CustomResponseStatus customResponseStatus, @Nullable Locale locale, @Nullable String messageCode, @Nullable T data) {
        if (messageCode != null) {
            Assert.isTrue(customResponseStatus == CustomResponseStatus.SUCCESS_WITH_INFORMATION
                    || customResponseStatus == CustomResponseStatus.SUCCESS_WITH_WARNING, """
                    'customResponseStatus' must be either of CustomResponseStatus.SUCCESS_WITH_INFORMATION
                    or CustomResponseStatus.SUCCESS_WITH_WARNING when msg is specified""");
        }

//        this.customResponseStatus = customResponseStatus;
        this.status = customResponseStatus.status;
        Locale usedLocale = (locale != null) ? locale : LocaleContextHolder.getLocale();

        if (messageCode != null) {
            String additionalMsg;
            try {
                additionalMsg = SimpleMessageInterpolator.getMessage(Constants.MessageSources.MESSAGES, usedLocale, customResponseStatus.messageCode, messageCode);
            } catch (Exception e) {
                additionalMsg = "none";
            }

            this.message = SimpleMessageInterpolator.getMessage(Constants.MessageSources.CUSTOM_RESPONSE_STATUS, usedLocale, customResponseStatus.messageCode, additionalMsg);
        } else {
            this.message = SimpleMessageInterpolator.getMessage(Constants.MessageSources.CUSTOM_RESPONSE_STATUS, usedLocale, customResponseStatus.messageCode);
        }

        this.data = data;
    }

    /**
     * Provide full customization capability to response body without conforming to {@link CustomResponseStatus} enum.
     * This should be used only for experiment or test.
     */
    public BaseResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> BaseResponse<T> succeeded() {
        return new BaseResponse<>();
    }

    public static <T> BaseResponse<T> succeeded(T data) {
        return new BaseResponse<>(data);
    }

    public static <T> BaseResponse<T> succeededWithAdditionalInformation(T data, String messageCode) {
        return new BaseResponse<>(CustomResponseStatus.SUCCESS_WITH_INFORMATION, null, messageCode, data);
    }

    public static <T> BaseResponse<T> succeededWithAdditionalInformation(T data, Locale locale, String messageCode) {
        return new BaseResponse<>(CustomResponseStatus.SUCCESS_WITH_INFORMATION, locale, messageCode, data);
    }

    public static <T> BaseResponse<T> succeededWithWarning(T data, String messageCode) {
        return new BaseResponse<>(CustomResponseStatus.SUCCESS_WITH_WARNING, null, messageCode, data);
    }

    public static <T> BaseResponse<T> succeededWithWarning(T data, Locale locale, String messageCode) {
        return new BaseResponse<>(CustomResponseStatus.SUCCESS_WITH_WARNING, locale, messageCode, data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseResponse<?> that)) return false;
        return status == that.status && Objects.equals(message, that.message) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, message, data);
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
