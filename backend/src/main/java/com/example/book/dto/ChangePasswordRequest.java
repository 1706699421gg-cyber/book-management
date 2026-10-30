package com.example.book.dto;

import lombok.Data;

/**
 * 修改本人密码请求
 */
@Data
public class ChangePasswordRequest {

    /** 原密码 */
    private String oldPassword;

    /** 新密码 */
    private String newPassword;
}
