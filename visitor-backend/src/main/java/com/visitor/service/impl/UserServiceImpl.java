package com.visitor.service.impl;

import com.visitor.entity.SysPermission;
import com.visitor.entity.SysUser;
import com.visitor.mapper.SysPermissionMapper;
import com.visitor.mapper.SysUserMapper;
import com.visitor.service.UserService;
import com.visitor.utils.JwtUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    public UserServiceImpl(SysUserMapper sysUserMapper,
                           SysPermissionMapper sysPermissionMapper,
                           JwtUtils jwtUtils,
                           BCryptPasswordEncoder passwordEncoder,
                           MessageSource messageSource) {
        this.sysUserMapper = sysUserMapper;
        this.sysPermissionMapper = sysPermissionMapper;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
    }

    @Override
    public Map<String, Object> login(String username, String password) {
        SysUser user = sysUserMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException(messageSource.getMessage("auth.bad_credentials", null, LocaleContextHolder.getLocale()));
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException(messageSource.getMessage("auth.account_disabled", null, LocaleContextHolder.getLocale()));
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException(messageSource.getMessage("auth.bad_credentials", null, LocaleContextHolder.getLocale()));
        }

        List<SysPermission> permissions = sysPermissionMapper.findByRoleId(user.getRoleId());
        List<String> permCodes = permissions.stream()
                .map(SysPermission::getPermCode)
                .collect(Collectors.toList());

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(),
                user.getRoleCode(), permCodes);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("roleCode", user.getRoleCode());
        userInfo.put("roleName", user.getRoleName());
        userInfo.put("permissions", permCodes);
        result.put("userInfo", userInfo);
        return result;
    }

    @Override
    public SysUser getCurrentUser(Long userId) {
        return sysUserMapper.findById(userId);
    }
}
