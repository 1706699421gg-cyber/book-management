package com.example.book.dto;

import lombok.Data;

/**
 * 管理员重置他人密码请求
 */
@Data
public class ResetPasswordRequest {

    /** 新密码 */
    private String newPassword;
}
