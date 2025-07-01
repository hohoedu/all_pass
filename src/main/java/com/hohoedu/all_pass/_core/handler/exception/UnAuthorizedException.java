package com.hohoedu.all_pass._core.handler.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class UnAuthorizedException extends RuntimeException {

    private HttpStatus status;

    public UnAuthorizedException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}
