package com.example.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.book.auth.AuthContext;
import com.example.book.auth.AuthTokenService;
import com.example.book.auth.PasswordHasher;
import com.example.book.auth.RolePermissionRegistry;
import com.example.book.common.BusinessException;
import com.example.book.dto.ChangePasswordRequest;
import com.example.book.dto.LoginRequest;
import com.example.book.dto.UserSaveRequest;
import com.example.book.entity.User;
import com.example.book.mapper.UserMapper;
import com.example.book.service.UserService;
import com.example.book.vo.LoginVO;
import com.example.book.vo.UserInfoVO;
import com.example.book.vo.UserVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户 / 登录 / 权限 Service 实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private AuthTokenService tokenService;

    @Resource
    private RolePermissionRegistry permissionRegistry;

    @Override
    public LoginVO login(LoginRequest request) {
        String username = request == null ? null : request.getUsername();
        String password = request == null ? null : request.getPassword();
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new BusinessException("用户名和密码不能为空");
        }
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username.trim()), false);
        // 统一提示, 避免泄露账号是否存在
        if (user == null || !PasswordHasher.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用, 请联系管理员");
        }
        String token = tokenService.createToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginVO(token, "Bearer", tokenService.getExpireSeconds(), buildUserInfo(user));
    }

    @Override
    public UserInfoVO currentUserInfo() {
        return buildUserInfo(requireCurrentUser());
    }

    @Override
    public Page<UserVO> pageUsers(long current, long size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(User::getUsername, kw).or().like(User::getRealName, kw));
        }
        wrapper.orderByAsc(User::getId);
        Page<User> page = this.page(new Page<>(current, size), wrapper);

        Page<UserVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO createUser(UserSaveRequest request) {
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        String username = requireText(request.getUsername(), "用户名");
        checkPassword(request.getPassword());
        String role = request.getRole() == null ? RolePermissionRegistry.ROLE_USER : request.getRole().trim();
        if (!permissionRegistry.isValidRole(role)) {
            throw new BusinessException("角色只能是 ADMIN 或 USER");
        }
        long sameName = this.count(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (sameName > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordHasher.encode(request.getPassword().trim()));
        user.setRealName(StringUtils.isBlank(request.getRealName()) ? username : request.getRealName().trim());
        user.setRole(role);
        user.setStatus(request.getStatus() != null && request.getStatus() == 0 ? 0 : 1);
        this.save(user);
        // 重新查询, 带回数据库默认填充的创建时间
        return toVO(this.getById(user.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUser(UserSaveRequest request) {
        if (request == null || request.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        User existing = this.getById(request.getId());
        if (existing == null) {
            throw new BusinessException("用户不存在或已被删除");
        }

        String username = StringUtils.isBlank(request.getUsername())
                ? existing.getUsername()
                : request.getUsername().trim();
        long sameName = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .ne(User::getId, existing.getId()));
        if (sameName > 0) {
            throw new BusinessException("用户名已存在");
        }

        String role = StringUtils.isBlank(request.getRole()) ? existing.getRole() : request.getRole().trim();
        if (!permissionRegistry.isValidRole(role)) {
            throw new BusinessException("角色只能是 ADMIN 或 USER");
        }
        Integer status = request.getStatus() != null && request.getStatus() == 0 ? 0 : 1;

        AuthContext.CurrentUser current = AuthContext.get();
        boolean editingSelf = current != null && current.id().equals(existing.getId());
        if (editingSelf) {
            if (status == 0) {
                throw new BusinessException("不能禁用当前登录的账号");
            }
            if (!existing.getRole().equals(role)) {
                throw new BusinessException("不能修改自己的角色");
            }
        }
        // 至少保留一名启用的管理员
        if (RolePermissionRegistry.ROLE_ADMIN.equals(existing.getRole())
                && (!RolePermissionRegistry.ROLE_ADMIN.equals(role) || status == 0)) {
            if (countEnabledAdmins() <= 1) {
                throw new BusinessException("系统至少需要保留一名启用的管理员");
            }
        }

        User update = new User();
        update.setId(existing.getId());
        update.setUsername(username);
        update.setRealName(StringUtils.isBlank(request.getRealName()) ? "" : request.getRealName().trim());
        update.setRole(role);
        update.setStatus(status);
        // 编辑时密码留空表示不修改
        if (StringUtils.isNotBlank(request.getPassword())) {
            checkPassword(request.getPassword());
            update.setPassword(PasswordHasher.encode(request.getPassword().trim()));
        }
        this.updateById(update);
        return toVO(this.getById(existing.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        User existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException("用户不存在或已被删除");
        }
        AuthContext.CurrentUser current = AuthContext.get();
        if (current != null && current.id().equals(id)) {
            throw new BusinessException("不能删除当前登录的账号");
        }
        if (RolePermissionRegistry.ROLE_ADMIN.equals(existing.getRole()) && countEnabledAdmins() <= 1) {
            throw new BusinessException("系统至少需要保留一名启用的管理员");
        }
        this.removeById(id);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        checkPassword(request.getNewPassword());
        User user = requireCurrentUser();
        if (!PasswordHasher.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        if (request.getOldPassword().trim().equals(request.getNewPassword().trim())) {
            throw new BusinessException("新密码不能与原密码相同");
        }
        User update = new User();
        update.setId(user.getId());
        update.setPassword(PasswordHasher.encode(request.getNewPassword().trim()));
        this.updateById(update);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        checkPassword(newPassword);
        User existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException("用户不存在或已被删除");
        }
        User update = new User();
        update.setId(existing.getId());
        update.setPassword(PasswordHasher.encode(newPassword.trim()));
        this.updateById(update);
    }

    // ------------------------------------------------------------------
    // 内部辅助
    // ------------------------------------------------------------------

    private User requireCurrentUser() {
        AuthContext.CurrentUser current = AuthContext.get();
        if (current == null) {
            throw new BusinessException("登录状态已失效, 请重新登录");
        }
        User user = this.getById(current.id());
        if (user == null) {
            throw new BusinessException("用户不存在或已被删除");
        }
        return user;
    }

    private long countEnabledAdmins() {
        return this.count(new LambdaQueryWrapper<User>()
                .eq(User::getRole, RolePermissionRegistry.ROLE_ADMIN)
                .eq(User::getStatus, 1));
    }

    private UserInfoVO buildUserInfo(User user) {
        List<String> permissions = permissionRegistry.permissionsOf(user.getRole());
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setRoleLabel(permissionRegistry.labelOf(user.getRole()));
        vo.setStatus(user.getStatus());
        vo.setPermissions(permissions);
        vo.setMenus(permissionRegistry.menusOf(permissions));
        return vo;
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setRoleLabel(permissionRegistry.labelOf(user.getRole()));
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }

    private String requireText(String value, String label) {
        if (StringUtils.isBlank(value)) {
            throw new BusinessException(label + "不能为空");
        }
        return value.trim();
    }

    private void checkPassword(String password) {
        if (StringUtils.isBlank(password)) {
            throw new BusinessException("密码不能为空");
        }
        String trimmed = password.trim();
        if (trimmed.length() < 6 || trimmed.length() > 50) {
            throw new BusinessException("密码长度需为 6-50 位");
        }
    }
}
