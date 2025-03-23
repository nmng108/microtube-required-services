package nmng108.microtube.processor.dto.base;

/**
 * A list of base status codes (along with human-readable messages)
 * to send back to user as part of a response.
 */
public enum ErrorCode {
    E00000("E00000"),
    E00001("E00001"),
    E00002("E00002"),
    E00003("E00003"),
    E00004("E00004"),
    E00005("E00005"),
    E00006("E00006"),
    E00007("E00007"),
    E00008("E00008"),

    E20001("E20001");

//    UNAUTHORIZED(3, "Unauthorized"),
//    RESOURCE_NOT_FOUND(4, "Resource is not found"),
//    RESOURCE_EXISTED(5, "Resource has existed"),
//    INVALID_REQUEST(6, "Invalid request"),
//    CANNOT_DELETE_RECORD_DUE_TO_DATA_CONSTRAINTS(7, "Cannot delete due to data constraint violation");

    public final String code;
//    public final String message;

    ErrorCode(String code) {
        this.code = code;
//        this.message = messageSource.getMessage(code);
    }
}
