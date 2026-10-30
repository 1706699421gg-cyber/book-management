package com.example.book.auth;

/**
 * 权限编码常量: 后端注解、角色配置、前端按钮都使用同一套编码
 */
public final class PermissionCodes {

    /** 数据统计 */
    public static final String STATS_VIEW = "stats:view";

    /** 图书 */
    public static final String BOOK_VIEW = "book:view";
    public static final String BOOK_ADD = "book:add";
    public static final String BOOK_EDIT = "book:edit";
    public static final String BOOK_DELETE = "book:delete";

    /** 分类 */
    public static final String CATEGORY_VIEW = "category:view";
    public static final String CATEGORY_ADD = "category:add";
    public static final String CATEGORY_EDIT = "category:edit";
    public static final String CATEGORY_DELETE = "category:delete";

    /** 用户管理 */
    public static final String USER_MANAGE = "user:manage";

    /** 个人中心 */
    public static final String PROFILE_VIEW = "profile:view";

    private PermissionCodes() {
    }
}
