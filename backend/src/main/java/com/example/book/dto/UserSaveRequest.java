package com.example.book.dto;

import lombok.Data;

/**
 * 新增 / 编辑用户请求(管理员使用)
 */
@Data
public class UserSaveRequest {

    /** 主键, 新增时为空 */
    private Long id;

    /** 登录账号 */
    private String username;

    /** 登录密码, 新增时必填; 编辑时留空表示不修改 */
    private String password;

    /** 真实姓名 / 昵称 */
    private String realName;

    /** 角色: ADMIN / USER */
    private String role;

    /** 状态: 1 启用 / 0 禁用 */
    private Integer status;
}
