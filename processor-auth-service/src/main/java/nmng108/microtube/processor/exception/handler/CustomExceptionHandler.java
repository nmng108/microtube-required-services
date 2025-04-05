package nmng108.microtube.processor.exception.handler;

import lombok.extern.slf4j.Slf4j;
import nmng108.microtube.processor.dto.base.ExceptionResponse;
import nmng108.microtube.processor.exception.CustomHttpException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {
//    /**
//     * An exception caused by user's invalid request data; manually thrown in code
//     *
//     * @param e InvalidRequestException
//     * @return 400
//     */
//    @ExceptionHandler({BadRequestException.class})
//    public ResponseEntity<ExceptionResponse> handleInvalidRequest(BadRequestException e) {
//        return e.toResponse();
//    }

    /**
     * An exception caused by user's invalid request data; manually thrown in code
     *
     * @param e InvalidRequestException
     * @return 400
     */
    @ExceptionHandler({CustomHttpException.class})
    public ResponseEntity<ExceptionResponse> handleCustomHttpException(CustomHttpException e) {
        return e.toResponse();
    }
}
