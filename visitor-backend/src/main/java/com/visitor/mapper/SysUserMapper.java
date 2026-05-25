package com.visitor.mapper;

import com.visitor.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysUserMapper {
    SysUser findByUsername(String username);
    SysUser findById(Long id);
    List<SysUser> findAll();
}
