package com.visitor.service;

import com.visitor.dto.AppointmentReviewDTO;
import com.visitor.dto.AppointmentSubmitDTO;
import com.visitor.entity.Appointment;

import java.util.List;
import java.util.Map;

public interface AppointmentService {
    void submitAppointment(AppointmentSubmitDTO dto);
    List<Appointment> queryByPhone(String phone);
    Map<String, Object> getPage(int page, int pageSize, Integer status, String keyword);
    void review(Long id, AppointmentReviewDTO dto, Long reviewerId);
}
