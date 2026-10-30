package com.example.book.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.book.dto.ChangePasswordRequest;
import com.example.book.dto.LoginRequest;
import com.example.book.dto.UserSaveRequest;
import com.example.book.entity.User;
import com.example.book.vo.LoginVO;
import com.example.book.vo.UserInfoVO;
import com.example.book.vo.UserVO;

/**
 * 用户 / 登录 / 权限 Service
 */
public interface UserService extends IService<User> {

    /**
     * 登录校验: 用户名密码正确且账号启用时返回令牌与权限信息
     */
    LoginVO login(LoginRequest request);

    /**
     * 当前登录用户信息(含权限与菜单)
     */
    UserInfoVO currentUserInfo();

    /**
     * 用户分页查询(管理员)
     */
    Page<UserVO> pageUsers(long current, long size, String keyword);

    /**
     * 新增用户(管理员)
     */
    UserVO createUser(UserSaveRequest request);

    /**
     * 编辑用户(管理员)
     */
    UserVO updateUser(UserSaveRequest request);

    /**
     * 删除用户(管理员)
     */
    void deleteUser(Long id);

    /**
     * 修改本人密码
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * 管理员重置他人密码
     */
    void resetPassword(Long id, String newPassword);
}
