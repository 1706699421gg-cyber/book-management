package com.example.book.auth;

/**
 * 当前请求的登录用户上下文(ThreadLocal)
 * 由 AuthInterceptor 在请求进入时写入, 请求结束时清理
 */
public final class AuthContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    /**
     * 当前登录用户, 未登录时为 null
     */
    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 当前用户是否拥有指定权限
     */
    public static boolean hasPermission(String permission) {
        CurrentUser current = HOLDER.get();
        return current != null && current.has(permission);
    }

    /**
     * 登录用户快照
     */
    public record CurrentUser(Long id,
                              String username,
                              String realName,
                              String role,
                              java.util.List<String> permissions) {

        public boolean has(String permission) {
            return permissions != null && permissions.contains(permission);
        }
    }
}
