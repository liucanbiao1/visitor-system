package com.visitor.mapper;

import com.visitor.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysRoleMapper {
    SysRole findById(Long id);
}
