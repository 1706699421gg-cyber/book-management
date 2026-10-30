package com.example.book.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.book.auth.PermissionCodes;
import com.example.book.auth.RequirePermission;
import com.example.book.common.Result;
import com.example.book.dto.ResetPasswordRequest;
import com.example.book.dto.UserSaveRequest;
import com.example.book.service.UserService;
import com.example.book.vo.UserVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理接口(仅管理员可访问)
 */
@RestController
@RequestMapping("/api/user")
@RequirePermission(PermissionCodes.USER_MANAGE)
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户分页 + 关键字搜索(用户名/姓名)
     * GET /api/user/page?current=1&size=10&keyword=xx
     */
    @GetMapping("/page")
    public Result<Page<UserVO>> page(@RequestParam(defaultValue = "1") long current,
                                     @RequestParam(defaultValue = "10") long size,
                                     @RequestParam(required = false) String keyword) {
        return Result.success(userService.pageUsers(current, size, keyword));
    }

    /**
     * 新增用户
     * POST /api/user
     */
    @PostMapping
    public Result<UserVO> add(@RequestBody UserSaveRequest request) {
        return Result.success("新增成功", userService.createUser(request));
    }

    /**
     * 编辑用户(姓名 / 角色 / 状态 / 可选密码)
     * PUT /api/user
     */
    @PutMapping
    public Result<UserVO> update(@RequestBody UserSaveRequest request) {
        return Result.success("修改成功", userService.updateUser(request));
    }

    /**
     * 删除用户
     * DELETE /api/user/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功", null);
    }

    /**
     * 重置他人密码
     * PUT /api/user/{id}/password
     */
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request == null ? null : request.getNewPassword());
        return Result.success("密码已重置", null);
    }
}
