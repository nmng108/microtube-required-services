package nmng108.microtube.processor.dto.base;

import nmng108.microtube.processor.util.SimpleMessageInterpolator;
import nmng108.microtube.processor.util.constant.Constants;

/**
 * A list of base status codes (along with human-readable messages)
 * to send back to user as part of a response.
 */
//@Getter
public enum CustomResponseStatus {
    SUCCESS("success.status", "success.message"),
    SUCCESS_WITH_INFORMATION("success-with-info.status", "success-with-info.message"),
    SUCCESS_WITH_WARNING("success-with-warning.status", "success-with-warning.message"),
    FAILED("failed.status", "failed.message");

    public final int status;
    public final String messageCode;

    CustomResponseStatus(String statusCode, String messageCode) {
        this.status = Integer.parseInt(SimpleMessageInterpolator.getMessage(Constants.MessageSources.CUSTOM_RESPONSE_STATUS, statusCode));
        this.messageCode = messageCode;
    }
}
