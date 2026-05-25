package com.visitor.service;

import com.visitor.entity.AccessLog;

import java.util.List;
import java.util.Map;

public interface AccessLogService {

    Map<String, Object> recordEntry(Long visitorId, Long appointmentId, String deviceName);

    void recordExit(Long id);

    Map<String, Object> getPage(int page, int pageSize, String keyword, Integer accessStatus);

    List<AccessLog> getOnCampusList();

    List<AccessLog> getOverstayAlerts();
}
