package org.timekeeper.controller.advice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.timekeeper.exception.BadRequestException;
import org.timekeeper.exception.DuplicateRequestException;
import org.timekeeper.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    private static final String EXCEPTION_MESSAGE = "exceptionMessage";

    private static final GlobalExceptionHandler HANDLER = new GlobalExceptionHandler();

    @Test
    public void testHandle_withResourceNotFoundException_returnsNotFoundStatus() {
        ResourceNotFoundException exception = new ResourceNotFoundException(EXCEPTION_MESSAGE);
        ResponseEntity<GlobalExceptionHandler.ExceptionDetails> expected = new ResponseEntity<>(
            GlobalExceptionHandler.ExceptionDetails.builder()
                .type(ResourceNotFoundException.class)
                .message(EXCEPTION_MESSAGE)
                .build(),
            HttpStatus.NOT_FOUND
        );

        assertEquals(
            expected,
            HANDLER.handle(exception, null)
        );
    }

    @Test
    public void testHandle_withBadRequestExceptionException_returnsBadRequestStatus() {
        BadRequestException exception = new BadRequestException(EXCEPTION_MESSAGE);
        ResponseEntity<GlobalExceptionHandler.ExceptionDetails> expected = new ResponseEntity<>(
            GlobalExceptionHandler.ExceptionDetails.builder()
                .type(BadRequestException.class)
                .message(EXCEPTION_MESSAGE)
                .build(),
            HttpStatus.BAD_REQUEST
        );

        assertEquals(
            expected,
            HANDLER.handle(exception, null)
        );
    }

    @Test
    public void testHandle_withDuplicateRequestException_returnsTooEarlyStatus() {
        DuplicateRequestException exception = new DuplicateRequestException(EXCEPTION_MESSAGE);
        ResponseEntity<GlobalExceptionHandler.ExceptionDetails> expected = new ResponseEntity<>(
            GlobalExceptionHandler.ExceptionDetails.builder()
                .type(DuplicateRequestException.class)
                .message(EXCEPTION_MESSAGE)
                .build(),
            HttpStatus.TOO_EARLY
        );

        assertEquals(
            expected,
            HANDLER.handle(exception, null)
        );
    }

}
