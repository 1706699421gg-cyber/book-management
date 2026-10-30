package com.example.book.auth;

import com.example.book.vo.MenuVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 角色 -> 权限 / 菜单 的集中配置
 *
 * 新增角色或调整权限时只需修改本类, 接口注解、菜单下发、前端按钮控制会自动生效。
 */
@Component
public class RolePermissionRegistry {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    /** 管理员: 全部权限 */
    private static final List<String> ADMIN_PERMISSIONS = List.of(
            PermissionCodes.STATS_VIEW,
            PermissionCodes.BOOK_VIEW,
            PermissionCodes.BOOK_ADD,
            PermissionCodes.BOOK_EDIT,
            PermissionCodes.BOOK_DELETE,
            PermissionCodes.CATEGORY_VIEW,
            PermissionCodes.CATEGORY_ADD,
            PermissionCodes.CATEGORY_EDIT,
            PermissionCodes.CATEGORY_DELETE,
            PermissionCodes.USER_MANAGE,
            PermissionCodes.PROFILE_VIEW
    );

    /** 普通用户: 只读(统计 / 图书 / 分类) + 个人中心 */
    private static final List<String> USER_PERMISSIONS = List.of(
            PermissionCodes.STATS_VIEW,
            PermissionCodes.BOOK_VIEW,
            PermissionCodes.CATEGORY_VIEW,
            PermissionCodes.PROFILE_VIEW
    );

    /** 全量菜单(按权限过滤后下发) */
    private static final List<MenuVO> ALL_MENUS = List.of(
            new MenuVO("/dashboard", "数据统计", "DataAnalysis", PermissionCodes.STATS_VIEW),
            new MenuVO("/books", "图书管理", "Reading", PermissionCodes.BOOK_VIEW),
            new MenuVO("/categories", "分类管理", "Collection", PermissionCodes.CATEGORY_VIEW),
            new MenuVO("/users", "用户管理", "UserFilled", PermissionCodes.USER_MANAGE),
            new MenuVO("/profile", "个人中心", "Avatar", PermissionCodes.PROFILE_VIEW)
    );

    /**
     * 角色对应的权限列表
     */
    public List<String> permissionsOf(String role) {
        if (ROLE_ADMIN.equals(role)) {
            return ADMIN_PERMISSIONS;
        }
        if (ROLE_USER.equals(role)) {
            return USER_PERMISSIONS;
        }
        // 未知角色默认无任何权限, 避免越权
        return List.of();
    }

    /**
     * 根据权限过滤菜单, 用于登录后自动分配功能菜单
     */
    public List<MenuVO> menusOf(List<String> permissions) {
        return ALL_MENUS.stream()
                .filter(menu -> permissions.contains(menu.getPermission()))
                .toList();
    }

    /**
     * 角色是否合法
     */
    public boolean isValidRole(String role) {
        return ROLE_ADMIN.equals(role) || ROLE_USER.equals(role);
    }

    /**
     * 角色中文名
     */
    public String labelOf(String role) {
        if (ROLE_ADMIN.equals(role)) {
            return "管理员";
        }
        if (ROLE_USER.equals(role)) {
            return "普通用户";
        }
        return "未知角色";
    }
}
