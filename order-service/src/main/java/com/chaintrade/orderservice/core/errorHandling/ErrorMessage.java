package com.chaintrade.orderservice.core.errorHandling;

import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class ErrorMessage {
    private final String message;
    private final ZonedDateTime time;

    public ErrorMessage(String message) {
        this.message = message;
        this.time = ZonedDateTime.now();
    }
}