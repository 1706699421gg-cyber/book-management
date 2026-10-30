package com.example.book.controller;

import com.example.book.common.Result;
import com.example.book.dto.ChangePasswordRequest;
import com.example.book.dto.LoginRequest;
import com.example.book.service.UserService;
import com.example.book.vo.LoginVO;
import com.example.book.vo.UserInfoVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录 / 当前用户接口
 * /api/auth/login 是唯一无需令牌的接口, 其余接口均需登录
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private UserService userService;

    /**
     * 登录: 校验用户名密码, 返回令牌 + 角色权限 + 可访问菜单
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginRequest request) {
        return Result.success("登录成功", userService.login(request));
    }

    /**
     * 获取当前登录用户信息(刷新页面 / 路由跳转时校验登录态)
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public Result<UserInfoVO> me() {
        return Result.success(userService.currentUserInfo());
    }

    /**
     * 修改本人密码
     * PUT /api/auth/password
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return Result.success("密码修改成功, 请重新登录", null);
    }

    /**
     * 退出登录(令牌为无状态签名, 前端清除本地令牌即可)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("已退出登录", null);
    }
}
