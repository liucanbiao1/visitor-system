package com.visitor.mapper;

import com.visitor.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysConfigMapper {

    SysConfig findByKey(@Param("configKey") String configKey);

    int upsert(@Param("configKey") String configKey, @Param("configValue") String configValue,
               @Param("updatedBy") Long updatedBy);
}
