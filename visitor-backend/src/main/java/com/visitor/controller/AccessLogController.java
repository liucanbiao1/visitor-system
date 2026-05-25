package com.visitor.controller;

import com.visitor.annotation.RequirePermission;
import com.visitor.common.Result;
import com.visitor.entity.AccessLog;
import com.visitor.service.AccessLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/access-log")
public class AccessLogController {

    private final AccessLogService accessLogService;

    public AccessLogController(AccessLogService accessLogService) {
        this.accessLogService = accessLogService;
    }

    @PostMapping("/entry")
    @RequirePermission("access-log:list")
    public Result<Map<String, Object>> recordEntry(@RequestBody Map<String, Object> body) {
        Long visitorId = body.get("visitorId") != null ? Long.valueOf(body.get("visitorId").toString()) : null;
        Long appointmentId = null;
        if (body.get("appointmentId") != null) {
            appointmentId = Long.valueOf(body.get("appointmentId").toString());
        }
        String deviceName = (String) body.getOrDefault("deviceName", "正门");
        if (visitorId == null) {
            return Result.error("访客ID不能为空");
        }
        return Result.success(accessLogService.recordEntry(visitorId, appointmentId, deviceName));
    }

    @PutMapping("/{id}/exit")
    @RequirePermission("access-log:list")
    public Result<Void> recordExit(@PathVariable Long id) {
        accessLogService.recordExit(id);
        return Result.success();
    }

    @GetMapping("/page")
    @RequirePermission("access-log:list")
    public Result<Map<String, Object>> getPage(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int pageSize,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer accessStatus) {
        return Result.success(accessLogService.getPage(page, pageSize, keyword, accessStatus));
    }

    @GetMapping("/on-campus")
    @RequirePermission("access-log:list")
    public Result<List<AccessLog>> getOnCampus() {
        return Result.success(accessLogService.getOnCampusList());
    }

    @GetMapping("/overstay")
    @RequirePermission("access-log:list")
    public Result<List<AccessLog>> getOverstay() {
        return Result.success(accessLogService.getOverstayAlerts());
    }
}
