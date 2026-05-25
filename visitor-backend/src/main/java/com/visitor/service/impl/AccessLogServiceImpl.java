package com.visitor.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visitor.entity.AccessLog;
import com.visitor.entity.Visitor;
import com.visitor.mapper.AccessLogMapper;
import com.visitor.mapper.VisitorMapper;
import com.visitor.service.AccessLogService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccessLogServiceImpl implements AccessLogService {

    private final AccessLogMapper accessLogMapper;
    private final VisitorMapper visitorMapper;
    private final MessageSource messageSource;

    public AccessLogServiceImpl(AccessLogMapper accessLogMapper, VisitorMapper visitorMapper,
                                MessageSource messageSource) {
        this.accessLogMapper = accessLogMapper;
        this.visitorMapper = visitorMapper;
        this.messageSource = messageSource;
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    @Override
    public Map<String, Object> recordEntry(Long visitorId, Long appointmentId, String deviceName) {
        Map<String, Object> result = new HashMap<>();
        Visitor visitor = visitorMapper.findById(visitorId);
        if (visitor == null) {
            throw new IllegalArgumentException(msg("visitor.not_found"));
        }

        if (visitor.getStatus() != null && visitor.getStatus() == 0) {
            AccessLog alertLog = new AccessLog();
            alertLog.setVisitorId(visitorId);
            alertLog.setAppointmentId(appointmentId);
            alertLog.setEntryTime(LocalDateTime.now());
            alertLog.setAccessStatus(0);
            alertLog.setDeviceName(deviceName);
            alertLog.setFailReason(msg("access.blacklist"));
            accessLogMapper.insert(alertLog);
            result.put("alert", true);
            result.put("message", msg("access.blacklist.alert", visitor.getName()));
            result.put("accessLog", alertLog);
            return result;
        }

        AccessLog todayLog = accessLogMapper.findTodayEntryByVisitorId(visitorId);
        if (todayLog != null) {
            throw new IllegalArgumentException(msg("access.already_entered"));
        }

        AccessLog log = new AccessLog();
        log.setVisitorId(visitorId);
        log.setAppointmentId(appointmentId);
        log.setEntryTime(LocalDateTime.now());
        log.setAccessStatus(1);
        log.setDeviceName(deviceName);
        accessLogMapper.insert(log);
        result.put("alert", false);
        result.put("message", msg("access.entry_success"));
        result.put("accessLog", log);
        return result;
    }

    @Override
    public void recordExit(Long id) {
        AccessLog log = accessLogMapper.findById(id);
        if (log == null) {
            throw new IllegalArgumentException(msg("access.record_not_found"));
        }
        if (log.getExitTime() != null) {
            throw new IllegalArgumentException(msg("access.already_exited"));
        }
        accessLogMapper.updateExitTime(id, LocalDateTime.now());
    }

    @Override
    public Map<String, Object> getPage(int page, int pageSize, String keyword, Integer accessStatus) {
        PageHelper.startPage(page, pageSize);
        List<AccessLog> list = accessLogMapper.findPage(keyword, accessStatus);
        PageInfo<AccessLog> pageInfo = new PageInfo<>(list);
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        result.put("page", pageInfo.getPageNum());
        result.put("pageSize", pageInfo.getPageSize());
        return result;
    }

    @Override
    public List<AccessLog> getOnCampusList() {
        return accessLogMapper.findOnCampus();
    }

    @Override
    public List<AccessLog> getOverstayAlerts() {
        return accessLogMapper.findOverstayLogs(8);
    }
}
