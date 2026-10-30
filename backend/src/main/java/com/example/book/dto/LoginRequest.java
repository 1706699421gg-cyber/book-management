package com.example.book.dto;

import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginRequest {

    /** 登录账号 */
    private String username;

    /** 登录密码 */
    private String password;
}
