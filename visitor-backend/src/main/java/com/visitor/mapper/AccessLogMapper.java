package com.visitor.mapper;

import com.visitor.entity.AccessLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AccessLogMapper {

    int insert(AccessLog accessLog);

    AccessLog findById(@Param("id") Long id);

    List<AccessLog> findPage(@Param("keyword") String keyword,
                             @Param("accessStatus") Integer accessStatus);

    int updateExitTime(@Param("id") Long id, @Param("exitTime") LocalDateTime exitTime);

    AccessLog findTodayEntryByVisitorId(@Param("visitorId") Long visitorId);

    List<AccessLog> findOnCampus();

    List<AccessLog> findOverstayLogs(@Param("thresholdHour") int thresholdHour);

    // statistics queries
    int countTodayVisitors();

    int countOnCampusVisitors();

    int countMonthlyVisitors();
}
