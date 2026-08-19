package com.permission.common.web;

import lombok.Data;

/**
 * 统一返回体。
 * 结构：{ code, message, data }，业务码见 {@link ResultCode}。
 *
 * @param <T> 数据类型
 */
@Data
public class R<T> {

    /** 业务码 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 数据 */
    private T data;

    /**
     * 构造返回体。
     *
     * @param code    业务码
     * @param message 提示信息
     * @param data    数据
     */
    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功返回。
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> R<T> success(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 失败返回。
     *
     * @param code    业务码
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> R<T> error(int code, String message) {
        return new R<>(code, message, null);
    }

    /**
     * 失败返回（按业务码枚举）。
     *
     * @param resultCode 业务码枚举
     * @param <T>        数据类型
     * @return 失败响应
     */
    public static <T> R<T> error(ResultCode resultCode) {
        return new R<>(resultCode.getCode(), resultCode.getMessage(), null);
    }
}
