package com.visitor.service;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    Map<String, Object> getOverview();

    List<Map<String, Object>> getTrafficStats(String period);

    List<Map<String, Object>> getTimeDistribution();

    List<Map<String, Object>> getReasonDistribution();
}
