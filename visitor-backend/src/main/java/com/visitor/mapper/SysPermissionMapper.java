package com.visitor.mapper;

import com.visitor.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysPermissionMapper {
    List<SysPermission> findByRoleId(Long roleId);
    List<SysPermission> findAll();
}
