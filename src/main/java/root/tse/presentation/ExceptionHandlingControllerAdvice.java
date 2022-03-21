package root.tse.presentation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlingControllerAdvice {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpectedException(Exception e) {
        return formMessage("Internal server error: %s.", e);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            ConstraintViolationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(Exception e) {
        return formMessage("Validation exception: %s.", e);
    }

    private String formMessage(String messageFormat, Exception e) {
        var errorMessage = String.format(messageFormat, e.getMessage());
        log.error(errorMessage, e);
        return errorMessage;
    }
}
