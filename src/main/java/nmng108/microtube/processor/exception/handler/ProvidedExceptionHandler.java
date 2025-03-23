package nmng108.microtube.processor.exception.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nmng108.microtube.processor.dto.base.BaseResponse;
import nmng108.microtube.processor.dto.base.ErrorCode;
import nmng108.microtube.processor.dto.base.ExceptionResponse;
import nmng108.microtube.processor.exception.BadRequestException;
import nmng108.microtube.processor.exception.InternalServerException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ProvidedExceptionHandler {
    private static final String MESSAGE_DELIMITER = "$$";
    private final MessageSource messageSource;

    /**
     * @param e UsernameNotFoundException
     * @return 401 HTTP status
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public BaseResponse<?> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return new ExceptionResponse(ErrorCode.E00007);
    }

    /**
     * @param e LockedException
     * @return 403 HTTP status
     */
    @ExceptionHandler(LockedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public BaseResponse<?> handleAccountBeingLockedException(LockedException e) {
//        this.logError(e);
        return null;
//        return new FailureResponse("E21", "User is locked");
    }

    /**
     * Manually thrown when the resource is not found
     *
     * @param e 404
     * @return
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse<?>> handleSpringNoServletResourceFoundException(NoResourceFoundException e) {
        String reason = messageSource.getMessage("errors.resource-path-not-found",
                new Object[]{e.getResourcePath()}, e.getMessage(), LocaleContextHolder.getLocale());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionResponse(ErrorCode.E00004, Collections.singletonList(reason)));
    }

    /**
     * Identical to ResourceNotFoundException.
     *
     * @return 404
     */
    @ExceptionHandler(NoSuchFileException.class)
    public ResponseEntity<ExceptionResponse> handleStoragePathNotFound(NoSuchFileException e) {
//        log.info("({}) {}", e.getClass().getCanonicalName(), e.getMessage());

        return null;
//        return new InvalidRequestException(e.getMessage()).toResponse();
    }

    /**
     * @param e PropertyReferenceException
     * @return 400
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ExceptionResponse> handleMismatchPropertyName(PropertyReferenceException e) {
//        log.info("({}) {}", e.getClass().getCanonicalName(), e.getMessage());

        return null;
//        return new InvalidRequestException(e.getMessage()).toResponse();
    }

//    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
//    public ResponseEntity<ExceptionResponse> handleMismatchPropertyName(InvalidDataAccessApiUsageException e) {
//        log.error("InvalidDataAccessApiUsageException: {}", e.getMessage());
//        return this.handleInvalidRequest(new InvalidRequestException("Wrong field name"));
//    }

    /**
     * Thrown when request data is parsed by DataBinder (the most common case is @RequestBody @Valid) and violates 1 or several constraints (e.g. Bean Validation).
     *
     * @return 400 - Bad request
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ExceptionResponse> handleDataBindingValidationException(MethodArgumentNotValidException e) {
        Class<?> dtoClass = e.getParameter().getParameterType();
//        String messageDelimiter = messageSource.getMessage("validation.messages.delimiter", new Object[]{}, MESSAGE_DELIMITER, LocaleContextHolder.getLocale());

        // [{field, message},...]
        return Optional.of(
                        e.getFieldErrors().stream().map((fieldError) ->
                                Optional.of(0)
                                        .map((ignored) -> {
                                            try {
                                                log.info("Class name: {}", dtoClass.getName());
                                                return dtoClass.getDeclaredField(fieldError.getField());
                                            } catch (NoSuchFieldException ex) {
                                                throw new RuntimeException(ex);
                                            }
                                        })
                                        .map((field) -> field.getAnnotation(JsonProperty.class))
                                        .map(JsonProperty::value)
                                        .or(() -> Optional.of(fieldError.getField()))
                                        .map((propertyName) -> new ExceptionResponse.ViolatedField(propertyName, fieldError.getDefaultMessage()))
                                        .get()
                        ).toList()
                )
                .map((details) -> new BadRequestException(ErrorCode.E00002, details))
                .map(BadRequestException::toResponse)
                .get();
    }

    /**
     * Thrown when request data is only parsed by MessageResolvers (the most common case is @Validated) and violates 1 or several constraints (e.g. Bean Validation).
     *
     * @return 400 - Bad request
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionResponse> handleConstraintValidation(ConstraintViolationException e) {
//        log.info("({}) {}", e.getClass().getCanonicalName(), e.getMessage());
        return Optional.of(
                        e.getConstraintViolations().stream()
                                .map((violation) -> {
                                    String[] splitPropertyPath = violation.getPropertyPath().toString().split("\\.");
                                    String methodName = splitPropertyPath[splitPropertyPath.length - 2];
                                    String propertyName = splitPropertyPath[splitPropertyPath.length - 1];
                                    Class<?>[] paramTypes = Stream.of(violation.getExecutableParameters()).map(Object::getClass).toArray(Class[]::new);
                                    Method method;

                                    method = Stream.of(violation.getRootBeanClass().getDeclaredMethods())
                                            .filter((m) -> m.getName().equals(methodName))
                                            .filter((m) -> isSameParameters(Stream.of(m.getParameters()).map(Parameter::getType).toArray(Class<?>[]::new), paramTypes))
                                            .findFirst()
                                            .orElseThrow(() -> new InternalServerException(STR."Cannot determine controller method for violated param \"\{violation.getRootBeanClass().getName()}.\{violation.getPropertyPath().toString()}"))
                                            ;

                                    return new ExceptionResponse.ViolatedField(getExposedParamName(method, propertyName), violation.getMessage());
                                })
                                .toList()
                )
                .map((responseDetails) -> new BadRequestException(ErrorCode.E00002, responseDetails))
                .map(BadRequestException::toResponse)
                .get();
    }

    private boolean isSameParameters(Class<?>[] a0, Class<?>[] a1) {
        if (a0.length != a1.length) {
            return false;
        }

        int assignedSide = -1; // -1 is initial state, determined by the first couple of param; 0 means all of a0 are assignable from a1, and inversely for 1

        for (int i = 0; i < a0.length; i++) {
            Class<?> p0 = a0[i];
            Class<?> p1 = a1[i];

            if (p0.isAssignableFrom(p1)) {
                if (assignedSide == -1) {
                    assignedSide = 0;
                } else if (assignedSide == 1) {
                    return false;
                }
            } else if (p1.isAssignableFrom(p0)) {
                if (assignedSide == -1) {
                    assignedSide = 1;
                } else if (assignedSide == 0) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * This method maps the internal parameter name to the exposed name from
     * {@link RequestParam}, {@link PathVariable}, {@link RequestPart} annotations.
     */
    @SneakyThrows
    private String getExposedParamName(Method method, String propertyName) {
        // Loop over the method parameters and their annotations
        return Stream.of(method.getParameters())
                .filter((parameter) -> parameter.getName().equals(propertyName))
                .findFirst()
                .map((parameter) -> {
                    for (Annotation annotation : parameter.getAnnotations()) {
                        if (annotation instanceof RequestParam || annotation instanceof PathVariable || annotation instanceof RequestPart) {
                            try {
                                return (String) annotation.getClass().getMethod("value").invoke(annotation);
                            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    return null;
                })
                .orElse("");
    }

    /**
     * An exception happens when request body format does not follow the standard (e.g. JSON)
     * or parser cannot parse Enum/Date fields, ....
     *
     * @param e HttpMessageNotReadableException
     * @return 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleUnreadableRequest(HttpMessageNotReadableException e) throws Exception {
        return ResponseEntity.badRequest().body(new ExceptionResponse(ErrorCode.E00001));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.badRequest().body(new ExceptionResponse(ErrorCode.E00000));
    }

    /**
     * Catch any exception other than the exceptions declared above.
     * <p>
     * By default, we consider all other exceptions are Internal server errors.
     *
     * @return 500 - Internal server error
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<ExceptionResponse> handleCommonExceptions(Exception e) {
//        log.info("({}) {}", e.getClass().getCanonicalName(), e.getMessage());
        // trace error
        e.printStackTrace();

        // WARNING: Responding with internal error message like this is discouraged and should only be conducted in dev env,
        // so remove the message or replace it with another appropriate one in production.
        return new InternalServerException(e.getMessage()).toResponse();
    }
}
