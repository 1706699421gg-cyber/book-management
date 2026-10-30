package com.example.book.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前端菜单项: 由后端按角色权限下发, 保证菜单与接口权限一致
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuVO {

    /** 路由地址 */
    private String path;

    /** 菜单标题 */
    private String title;

    /** Element Plus 图标组件名 */
    private String icon;

    /** 访问该菜单所需的权限编码 */
    private String permission;
}
