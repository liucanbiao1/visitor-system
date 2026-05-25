package com.visitor.mapper;

import com.visitor.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AppointmentMapper {
    int insert(Appointment appointment);
    Appointment findById(Long id);
    List<Appointment> findPage(@Param("status") Integer status, @Param("keyword") String keyword);
    List<Appointment> findByPhone(@Param("phone") String phone);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status,
                     @Param("rejectReason") String rejectReason,
                     @Param("reviewerId") Long reviewerId,
                     @Param("reviewTime") LocalDateTime reviewTime);
}
