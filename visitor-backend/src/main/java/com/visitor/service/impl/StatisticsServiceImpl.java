package com.visitor.service.impl;

import com.visitor.mapper.AccessLogMapper;
import com.visitor.service.StatisticsService;
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

    public StatisticsServiceImpl(JdbcTemplate jdbcTemplate, AccessLogMapper accessLogMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.accessLogMapper = accessLogMapper;
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
                sql = "SELECT CONCAT('第', WEEK(appointment_time), '周') AS label, COUNT(*) AS count " +
                      "FROM appointment WHERE appointment_time >= DATE_SUB(CURDATE(), INTERVAL 28 DAY) " +
                      "GROUP BY label ORDER BY label";
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
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("label", rs.getString("label"));
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
