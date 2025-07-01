package com.hohoedu.all_pass._core.handler.exception;

import org.springframework.http.HttpStatus;
import com.hohoedu.all_pass._core.utils.ApiUtils;
import lombok.Getter;

//UNAUTHORIZED (인증 실패)
@Getter
public class Exception401 extends RuntimeException {

    public Exception401(String message) {
        super(message);
    }

    public ApiUtils.ApiResult<?> body() {
        return ApiUtils.error(getMessage(), HttpStatus.UNAUTHORIZED);
    }

    public HttpStatus status() {
        return HttpStatus.UNAUTHORIZED;
    }

}
