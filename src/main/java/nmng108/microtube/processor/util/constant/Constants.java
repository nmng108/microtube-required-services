package nmng108.microtube.processor.util.constant;

public interface Constants {
    String DATABASE_NAME = "MICROTUBE";

    interface Paging {
        int DEFAULT_PAGE_NUMBER = 1;
        int DEFAULT_PAGE_SIZE = 20;
    }

    interface MessageSources {
        String MESSAGES = "messages";
        String CUSTOM_RESPONSE_STATUS = "custom-response-status";
        String ERROR_CODES = "error-codes";
        String ERRORS = "errors";
    }
}
