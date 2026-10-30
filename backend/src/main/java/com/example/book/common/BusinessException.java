package com.example.book.common;

/**
 * 业务异常: 由全局异常处理器统一捕获并返回给前端
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }
}
