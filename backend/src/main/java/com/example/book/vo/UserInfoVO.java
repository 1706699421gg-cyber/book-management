package com.example.book.vo;

import lombok.Data;

import java.util.List;

/**
 * 登录用户信息(含角色、权限、菜单), 供前端做路由与按钮级控制
 */
@Data
public class UserInfoVO {

    private Long id;

    private String username;

    private String realName;

    /** ADMIN / USER */
    private String role;

    /** 角色中文名 */
    private String roleLabel;

    /** 1 启用 / 0 禁用 */
    private Integer status;

    /** 权限编码列表 */
    private List<String> permissions;

    /** 可访问菜单 */
    private List<MenuVO> menus;
}
