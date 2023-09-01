package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDto errorNotFound(final NotFoundException e) {
        log.info(Arrays.toString(e.getStackTrace()));
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorDto errorAccessException(final ErrorAccess e) {
        log.info(Arrays.toString(e.getStackTrace()));
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public ErrorDto notImplementedException(final NotImplementedException e) {
        log.info(Arrays.toString(e.getStackTrace()));
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto validationException(final ValidationException e) {
        log.info(Arrays.toString(e.getStackTrace()));
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDto emailException(final EmailBusyException e) {
        log.info(Arrays.toString(e.getStackTrace()));
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto unknownStateException(UnknownStateException e) {
        log.info(Arrays.toString(e.getStackTrace()));
        return new ErrorDto("Unknown state: UNSUPPORTED_STATUS");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto handleThrowable(final Throwable e) {
        log.info(Arrays.toString(e.getStackTrace()));
        return new ErrorDto("Ой, что-то пошло не так");
    }
}
