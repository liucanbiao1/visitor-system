package com.visitor.service;

import com.visitor.entity.SysUser;

import java.util.Map;

public interface UserService {
    Map<String, Object> login(String username, String password);
    SysUser getCurrentUser(Long userId);
}
