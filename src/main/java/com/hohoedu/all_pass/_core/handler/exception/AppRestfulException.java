package com.hohoedu.all_pass._core.handler.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppRestfulException extends RuntimeException {

    private HttpStatus status;

    public AppRestfulException(String message, HttpStatus httpStatus) {
        super(message);
        this.status = httpStatus;
    }
}
