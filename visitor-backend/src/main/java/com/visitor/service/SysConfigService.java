package com.visitor.service;

public interface SysConfigService {

    boolean getBoolean(String key, boolean defaultValue);

    void setConfig(String key, String value, Long updatedBy);
}
