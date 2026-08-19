package com.permission.common.exception;

import com.permission.common.web.ResultCode;
import lombok.Getter;

/**
 * 业务异常。
 * 由全局异常处理器统一转换为 R 返回，禁止在业务代码中 try-catch 吞掉。
 */
@Getter
public class ServiceException extends RuntimeException {

    /** 业务码 */
    private final int code;

    /**
     * 按业务码枚举构造异常。
     *
     * @param resultCode 业务码枚举
     */
    public ServiceException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 按业务码 + 自定义提示构造异常。
     *
     * @param resultCode 业务码枚举
     * @param message    自定义提示
     */
    public ServiceException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
