package com.xinjia.coupon.common.auth;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.xinjia.coupon.common.enums.ErrorCode;
import com.xinjia.coupon.common.exception.BusinessException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HeaderAuthInterceptor implements HandlerInterceptor {

    private final AuthProperties authProperties;

    public HeaderAuthInterceptor(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!authProperties.isEnabled()) {
            return true;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/api/admin/")) {
            authenticateAdmin(request);
            return true;
        }
        if (path.startsWith("/api/user/") || path.startsWith("/api/settlement/")) {
            authenticateUser(request);
        }
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception
    ) {
        RequestIdentityHolder.clear();
    }

    private void authenticateAdmin(HttpServletRequest request) {
        String token = request.getHeader(authProperties.getAdminTokenHeader());
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException("缺少管理端访问令牌");
        }
        if (!token.equals(authProperties.getAdminToken())) {
            throw new ForbiddenException("管理端访问令牌无效");
        }
        RequestIdentityHolder.set(RequestIdentity.administrator());
    }

    private void authenticateUser(HttpServletRequest request) {
        String userIdText = request.getHeader(authProperties.getUserIdHeader());
        if (!StringUtils.hasText(userIdText)) {
            throw new UnauthorizedException("缺少用户身份请求头");
        }
        try {
            long userId = Long.parseLong(userIdText);
            if (userId <= 0) {
                throw new NumberFormatException("userId must be positive");
            }
            RequestIdentityHolder.set(RequestIdentity.user(userId));
        } catch (NumberFormatException exception) {
            throw new BusinessException(ErrorCode.PARAMETER_INVALID, "用户身份请求头必须是正整数");
        }
    }
}
