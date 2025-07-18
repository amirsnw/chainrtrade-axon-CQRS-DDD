package com.chaintrade.orderservice.core.errorHandling;

import org.axonframework.commandhandling.CommandExecutionException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class OrdersServiceErrorHandler {

    @ExceptionHandler(value = IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        return ResponseEntity.internalServerError().body(new ErrorMessage(ex.getMessage()));
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return ResponseEntity.badRequest().body(new ErrorMessage(ex.getMessage()));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleOtherExceptions(Exception ex, WebRequest request) {
        return ResponseEntity.internalServerError().body(new ErrorMessage(ex.getMessage()));
    }

    @ExceptionHandler(value = CommandExecutionException.class)
    public ResponseEntity<Object> handleCommandExecutionExceptions(CommandExecutionException ex, WebRequest request) {
        return ResponseEntity.internalServerError().body(new ErrorMessage(ex.getMessage()));
    }
}
