package com.example.book.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户列表展示对象(不含密码)
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String realName;

    /** ADMIN / USER */
    private String role;

    /** 角色中文名 */
    private String roleLabel;

    /** 1 启用 / 0 禁用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
