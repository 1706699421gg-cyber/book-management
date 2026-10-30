package com.example.book.config;

import com.example.book.auth.PasswordHasher;
import com.example.book.auth.RolePermissionRegistry;
import com.example.book.entity.User;
import com.example.book.mapper.UserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 首次启动时创建演示账号(仅在 t_user 表为空时执行, 不会覆盖已有账号)
 *   admin  / admin123   管理员
 *   reader / user123    普通用户
 */
@Slf4j
@Component
public class UserDataInitializer implements CommandLineRunner {

    @Resource
    private UserMapper userMapper;

    @Override
    public void run(String... args) {
        try {
            Long count = userMapper.selectCount(null);
            if (count != null && count > 0) {
                return;
            }
            userMapper.insert(buildUser("admin", "admin123", "系统管理员", RolePermissionRegistry.ROLE_ADMIN));
            userMapper.insert(buildUser("reader", "user123", "普通用户", RolePermissionRegistry.ROLE_USER));
            log.info("已创建默认账号: admin/admin123(管理员), reader/user123(普通用户)");
        } catch (Exception e) {
            log.warn("初始化默认账号失败(请先执行 db/upgrade_auth.sql 创建 t_user 表): {}", e.getMessage());
        }
    }

    private User buildUser(String username, String rawPassword, String realName, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordHasher.encode(rawPassword));
        user.setRealName(realName);
        user.setRole(role);
        user.setStatus(1);
        return user;
    }
}
