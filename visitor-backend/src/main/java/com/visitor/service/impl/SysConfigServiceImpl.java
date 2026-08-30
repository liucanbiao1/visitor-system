package com.visitor.service.impl;

import com.visitor.entity.SysConfig;
import com.visitor.mapper.SysConfigMapper;
import com.visitor.service.SysConfigService;
import org.springframework.stereotype.Service;

@Service
public class SysConfigServiceImpl implements SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    public SysConfigServiceImpl(SysConfigMapper sysConfigMapper) {
        this.sysConfigMapper = sysConfigMapper;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        SysConfig config = sysConfigMapper.findByKey(key);
        if (config == null || config.getConfigValue() == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(config.getConfigValue().trim());
    }

    @Override
    public void setConfig(String key, String value, Long updatedBy) {
        sysConfigMapper.upsert(key, value, updatedBy);
    }
}
