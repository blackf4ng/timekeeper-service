package org.timekeeper.controller.advice;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.timekeeper.exception.DuplicateRequestException;
import org.timekeeper.exception.BadRequestException;
import org.timekeeper.exception.ResourceNotFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    public static class ExceptionDetails {

        Class<? extends RuntimeException> type;

        String message;

    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ExceptionDetails> handle(ResourceNotFoundException exception, WebRequest request) {
        log.info("ResourceNotFoundException encountered; translating to external exception: message={}", exception.getMessage());

        return handle(exception, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionDetails> handle(BadRequestException exception, WebRequest request) {
        log.info("InvalidInputException encountered; translating to external exception: message={}", exception.getMessage());

        return handle(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<ExceptionDetails> handle(DuplicateRequestException exception, WebRequest request) {
        log.info("DuplicateRequestException encountered; translating to external exception: message={}", exception.getMessage());

        HttpStatus status = HttpStatus.TOO_EARLY;
        return handle(exception, status);
    }

    private ResponseEntity<ExceptionDetails> handle(
        RuntimeException exception,
        HttpStatus httpStatus
    ) {
        return handle(exception, exception.getMessage(), httpStatus);
    }

    private ResponseEntity<ExceptionDetails> handle(
        RuntimeException exception,
        String exceptionMessage,
        HttpStatus httpStatus
    ) {
        ExceptionDetails exceptionDetails = ExceptionDetails.builder()
            .type(exception.getClass())
            .message(exceptionMessage)
            .build();

        return new ResponseEntity<>(exceptionDetails, httpStatus);
    }

}
