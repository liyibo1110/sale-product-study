package com.github.liyibo1110.saleproduct.base.result;

import com.github.liyibo1110.saleproduct.base.exception.ErrorEnum;
import com.github.liyibo1110.saleproduct.base.exception.IErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Optional;

/**
 * 统一API响应结果
 * @author liyibo
 * @date 2026-07-02 11:05
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SUCCESS_CODE = "success";
    private static final String SUCCESS_MESSAGE = "操作成功";

    private String code;
    private String message;
    private T data;
    private Boolean success;

    private Result(String code, String message, T data, Boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
    }

    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, null, true);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, data, true);
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result<>(SUCCESS_CODE, message, data, true);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(ErrorEnum.SYSTEM_ERROR.getCode(), message, null, false);
    }

    public static <T> Result<T> fail(String code, String message) {
        return new Result<>(code, message, null, false);
    }

    public static <T> Result<T> fail(ErrorEnum errorEnum) {
        return new Result<>(errorEnum.getCode(), errorEnum.getMessage(), null, false);
    }

    public static <T> Result<T> fail(ErrorEnum errorEnum, String message) {
        return new Result<>(errorEnum.getCode(), message, null, false);
    }

    public static <T> Result<T> fail(IErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null, false);
    }

    public static boolean isSuccess(Result<?> result) {
        return Optional.ofNullable(result)
                .map(Result::getSuccess)
                .orElse(Boolean.FALSE);
    }

    public static boolean isFailed(Result<?> result) {
        return !isSuccess(result);
    }
}
