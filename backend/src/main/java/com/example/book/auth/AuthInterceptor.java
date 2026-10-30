package com.example.book.auth;

import com.example.book.common.Result;
import com.example.book.entity.User;
import com.example.book.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

/**
 * 全链路鉴权拦截器
 *
 * 1. 校验 Authorization 令牌(未登录 / 过期 / 被篡改 -> 401)
 * 2. 校验账号状态(被禁用 -> 401)
 * 3. 校验 @RequirePermission 注解声明的权限(无权限 -> 403)
 * 4. 将登录用户写入 AuthContext, 供业务层使用
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Resource
    private AuthTokenService tokenService;

    @Resource
    private RolePermissionRegistry permissionRegistry;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        // 浏览器跨域预检请求直接放行
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        AuthTokenService.TokenPayload payload = tokenService.parseToken(request.getHeader("Authorization"));
        if (payload == null) {
            writeError(response, 401, "登录状态已失效, 请重新登录");
            return false;
        }

        User user = userMapper.selectById(payload.userId());
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            writeError(response, 401, "账号不存在或已被禁用, 请重新登录");
            return false;
        }

        List<String> permissions = permissionRegistry.permissionsOf(user.getRole());
        AuthContext.set(new AuthContext.CurrentUser(
                user.getId(), user.getUsername(), user.getRealName(), user.getRole(), permissions));

        RequirePermission required = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (required == null) {
            required = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }
        if (required != null) {
            for (String permission : required.value()) {
                if (!permissions.contains(permission)) {
                    writeError(response, 403, "无权限执行该操作");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 线程复用, 必须清理, 避免权限信息串请求
        AuthContext.clear();
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(code, message)));
    }
}
