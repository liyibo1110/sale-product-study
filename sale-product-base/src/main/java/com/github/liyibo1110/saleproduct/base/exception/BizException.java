package com.github.liyibo1110.saleproduct.base.exception;

import lombok.Getter;

/**
 * 业务异常
 * @author liyibo
 * @date 2026-07-02 11:04
 */
@Getter
public class BizException extends RuntimeException {

    private final String code;
    private final String message;

    public BizException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BizException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public static BizException of(String errorCode, String errorMsg) {
        return new BizException(errorCode, errorMsg);
    }

    public static BizException of(IErrorCode errorCode) {
        return new BizException(errorCode.getCode(), errorCode.getMessage());
    }

    public static BizException of(ErrorEnum errorEnum) {
        return new BizException(errorEnum.getCode(), errorEnum.getMessage());
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
