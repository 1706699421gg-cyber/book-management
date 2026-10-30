package com.example.book.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结构
 * {
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": ...
 * }
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码: 200 成功, 500 失败 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 数据载荷 */
    private T data;

    public static <T> Result<T> success() {
        return build(200, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return build(200, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return build(200, message, data);
    }

    public static <T> Result<T> error(String message) {
        return build(500, message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return build(code, message, null);
    }

    private static <T> Result<T> build(Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}
