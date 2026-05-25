package com.visitor.config;

import com.visitor.annotation.RequirePermission;
import com.visitor.utils.JwtUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final MessageSource messageSource;

    public JwtInterceptor(JwtUtils jwtUtils, MessageSource messageSource) {
        this.jwtUtils = jwtUtils;
        this.messageSource = messageSource;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = extractToken(request);
        if (token == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            writeError(response, messageSource.getMessage("auth.not_logged_in", null,
                    LocaleContextHolder.getLocale()));
            return false;
        }

        if (!jwtUtils.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            writeError(response, messageSource.getMessage("auth.token_expired", null,
                    LocaleContextHolder.getLocale()));
            return false;
        }

        Long userId = jwtUtils.getUserId(token);
        String username = jwtUtils.getUsername(token);
        String roleCode = jwtUtils.getRoleCode(token);
        List<String> permissions = jwtUtils.getPermissions(token);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("roleCode", roleCode);
        request.setAttribute("permissions", permissions);

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequirePermission annotation = handlerMethod.getMethodAnnotation(RequirePermission.class);
            if (annotation == null) {
                annotation = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
            }
            if (annotation != null && !annotation.value().isEmpty()) {
                if (!permissions.contains(annotation.value())) {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    writeError(response, messageSource.getMessage("auth.no_permission", null,
                            LocaleContextHolder.getLocale()));
                    return false;
                }
            }
        }

        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void writeError(HttpServletResponse response, String message) {
        try {
            response.getWriter().write("{\"code\":" + response.getStatus()
                    + ",\"message\":\"" + message + "\",\"data\":null}");
        } catch (Exception ignored) {
        }
    }
}
