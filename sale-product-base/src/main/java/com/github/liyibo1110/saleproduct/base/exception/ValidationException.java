package com.github.liyibo1110.saleproduct.base.exception;

import lombok.Getter;

/**
 * @author liyibo
 * @date 2026-07-02 11:05
 */
@Getter
public class ValidationException extends RuntimeException {

    private final String code;
    private final String message;

    public ValidationException(String message) {
        super(message);
        this.code = ErrorEnum.PARAM_INVALID_VALUE.getCode();
        this.message = message;
    }

    public ValidationException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
