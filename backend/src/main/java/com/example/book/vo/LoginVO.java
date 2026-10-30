package com.example.book.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功返回: 令牌 + 用户信息(角色 / 权限 / 菜单)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    /** 访问令牌 */
    private String token;

    /** 令牌类型, 前端拼装为 Authorization: Bearer <token> */
    private String tokenType;

    /** 令牌有效期(秒) */
    private Long expiresIn;

    /** 登录用户信息 */
    private UserInfoVO user;
}
