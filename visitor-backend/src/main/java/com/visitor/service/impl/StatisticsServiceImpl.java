package com.visitor.service.impl;

import com.visitor.mapper.AccessLogMapper;
import com.visitor.service.StatisticsService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final JdbcTemplate jdbcTemplate;
    private final AccessLogMapper accessLogMapper;
    private final MessageSource messageSource;

    public StatisticsServiceImpl(JdbcTemplate jdbcTemplate, AccessLogMapper accessLogMapper, MessageSource messageSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.accessLogMapper = accessLogMapper;
        this.messageSource = messageSource;
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();
        result.put("todayVisitors", accessLogMapper.countTodayVisitors());
        result.put("onCampus", accessLogMapper.countOnCampusVisitors());
        result.put("monthlyVisitors", accessLogMapper.countMonthlyVisitors());
        Integer pendingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointment WHERE status = 0", Integer.class);
        result.put("pendingApprovals", pendingCount != null ? pendingCount : 0);
        return result;
    }

    @Override
    public List<Map<String, Object>> getTrafficStats(String period) {
        String sql;
        switch (period) {
            case "day":
                sql = "SELECT DATE(appointment_time) AS label, COUNT(*) AS count " +
                      "FROM appointment WHERE appointment_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                      "GROUP BY label ORDER BY label";
                break;
            case "week":
                sql = "SELECT WEEK(appointment_time) AS week_num, COUNT(*) AS count " +
                      "FROM appointment WHERE appointment_time >= DATE_SUB(CURDATE(), INTERVAL 28 DAY) " +
                      "GROUP BY week_num ORDER BY week_num";
                break;
            case "month":
                sql = "SELECT DATE_FORMAT(appointment_time, '%Y-%m') AS label, COUNT(*) AS count " +
                      "FROM appointment GROUP BY label ORDER BY label DESC LIMIT 12";
                break;
            default:
                sql = "SELECT DATE(appointment_time) AS label, COUNT(*) AS count " +
                      "FROM appointment WHERE appointment_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                      "GROUP BY label ORDER BY label";
        }
        final boolean isWeek = "week".equals(period);
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            String label = isWeek ? msg("statistics.week", rs.getInt("week_num")) : rs.getString("label");
            row.put("label", label);
            row.put("count", rs.getInt("count"));
            return row;
        });
    }

    @Override
    public List<Map<String, Object>> getTimeDistribution() {
        String sql = "SELECT DATE(appointment_time) AS date, " +
                     "SUM(CASE WHEN HOUR(appointment_time) BETWEEN 6 AND 11 THEN 1 ELSE 0 END) AS morning, " +
                     "SUM(CASE WHEN HOUR(appointment_time) BETWEEN 12 AND 17 THEN 1 ELSE 0 END) AS afternoon, " +
                     "SUM(CASE WHEN HOUR(appointment_time) BETWEEN 18 AND 23 THEN 1 ELSE 0 END) AS evening " +
                     "FROM appointment " +
                     "WHERE appointment_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                     "GROUP BY DATE(appointment_time) ORDER BY date";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("date", rs.getString("date"));
            row.put("morning", rs.getInt("morning"));
            row.put("afternoon", rs.getInt("afternoon"));
            row.put("evening", rs.getInt("evening"));
            return row;
        });
    }

    @Override
    public List<Map<String, Object>> getReasonDistribution() {
        String sql = "SELECT visit_reason AS name, COUNT(*) AS value " +
                     "FROM appointment GROUP BY visit_reason ORDER BY value DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("name", rs.getString("name"));
            row.put("value", rs.getInt("value"));
            return row;
        });
    }
}
