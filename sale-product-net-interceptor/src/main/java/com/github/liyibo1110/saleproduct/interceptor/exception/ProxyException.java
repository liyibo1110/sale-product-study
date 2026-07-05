package com.github.liyibo1110.saleproduct.interceptor.exception;

/**
 * @author liyibo
 * @date 2026-07-04 19:22
 */
public class ProxyException extends RuntimeException {

    private final int httpStatus;
    private final String errorCode;

    public ProxyException(int httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public ProxyException(int httpStatus, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public static ProxyException badGateway(String message) {
        return new ProxyException(502, "bad_gateway", message);
    }

    public static ProxyException badGateway(String message, Throwable cause) {
        return new ProxyException(502, "bad_gateway", message, cause);
    }

    public static ProxyException gatewayTimeout(String message) {
        return new ProxyException(504, "gateway_timeout", message);
    }

    public static ProxyException gatewayTimeout(String message, Throwable cause) {
        return new ProxyException(504, "gateway_timeout", message, cause);
    }

    public static ProxyException internal(String message) {
        return new ProxyException(500, "internal_error", message);
    }

    public static ProxyException internal(String message, Throwable cause) {
        return new ProxyException(500, "internal_error", message, cause);
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
