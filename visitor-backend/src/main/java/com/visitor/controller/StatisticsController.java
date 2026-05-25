package com.visitor.controller;

import com.visitor.annotation.RequirePermission;
import com.visitor.common.Result;
import com.visitor.service.StatisticsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/overview")
    @RequirePermission("dashboard")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(statisticsService.getOverview());
    }

    @GetMapping("/traffic")
    @RequirePermission("dashboard")
    public Result<List<Map<String, Object>>> getTrafficStats(@RequestParam(defaultValue = "day") String period) {
        return Result.success(statisticsService.getTrafficStats(period));
    }

    @GetMapping("/time-distribution")
    @RequirePermission("dashboard")
    public Result<List<Map<String, Object>>> getTimeDistribution() {
        return Result.success(statisticsService.getTimeDistribution());
    }

    @GetMapping("/reason-distribution")
    @RequirePermission("dashboard")
    public Result<List<Map<String, Object>>> getReasonDistribution() {
        return Result.success(statisticsService.getReasonDistribution());
    }
}
