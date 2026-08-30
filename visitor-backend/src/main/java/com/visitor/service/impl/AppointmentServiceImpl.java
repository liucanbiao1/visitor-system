package com.visitor.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.visitor.dto.AppointmentReviewDTO;
import com.visitor.dto.AppointmentSubmitDTO;
import com.visitor.entity.Appointment;
import com.visitor.entity.Visitor;
import com.visitor.mapper.AppointmentMapper;
import com.visitor.mapper.VisitorMapper;
import com.visitor.service.AiReviewService;
import com.visitor.service.AppointmentService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final VisitorMapper visitorMapper;
    private final MessageSource messageSource;
    private final AiReviewService aiReviewService;

    public AppointmentServiceImpl(AppointmentMapper appointmentMapper, VisitorMapper visitorMapper,
                                  MessageSource messageSource, AiReviewService aiReviewService) {
        this.appointmentMapper = appointmentMapper;
        this.visitorMapper = visitorMapper;
        this.messageSource = messageSource;
        this.aiReviewService = aiReviewService;
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    @Override
    public void submitAppointment(AppointmentSubmitDTO dto) {
        Visitor visitor = visitorMapper.findByPhone(dto.getPhone());
        if (visitor != null && visitor.getStatus() != null && visitor.getStatus() == 0) {
            throw new RuntimeException(msg("appointment.blacklist.phone"));
        }

        if (dto.getIdCard() != null && !dto.getIdCard().isEmpty()) {
            Visitor blacklisted = visitorMapper.findByIdCardAndStatus(dto.getIdCard(), 0);
            if (blacklisted != null) {
                throw new RuntimeException(msg("appointment.blacklist.idcard"));
            }
        }

        if (visitor != null) {
            boolean changed = false;
            if (dto.getName() != null && !dto.getName().equals(visitor.getName())) {
                visitor.setName(dto.getName());
                changed = true;
            }
            if (dto.getIdCard() != null && !dto.getIdCard().isEmpty()
                    && !dto.getIdCard().equals(visitor.getIdCard())) {
                visitor.setIdCard(dto.getIdCard());
                changed = true;
            }
            if (dto.getGender() != null && !dto.getGender().equals(visitor.getGender())) {
                visitor.setGender(dto.getGender());
                changed = true;
            }
            if (changed) {
                visitorMapper.update(visitor);
            }
        } else {
            visitor = new Visitor();
            visitor.setName(dto.getName());
            visitor.setPhone(dto.getPhone());
            visitor.setIdCard(dto.getIdCard() != null ? dto.getIdCard() : "");
            visitor.setGender(dto.getGender() != null ? dto.getGender() : 0);
            visitor.setStatus(1);
            visitorMapper.insert(visitor);
        }

        Appointment appointment = new Appointment();
        appointment.setVisitorId(visitor.getId());
        appointment.setAppointmentTime(dto.getAppointmentTime());
        appointment.setVisitReason(dto.getVisitReason());
        appointment.setReasonDetail(dto.getReasonDetail());
        appointment.setHostName(dto.getHostName());
        appointment.setHostDept(dto.getHostDept());
        appointment.setStatus(0);
        appointmentMapper.insert(appointment);

        aiReviewService.tryAutoReview(appointment.getId());
    }

    @Override
    public List<Appointment> queryByPhone(String phone) {
        return appointmentMapper.findByPhone(phone);
    }

    @Override
    public Map<String, Object> getPage(int page, int pageSize, Integer status, String keyword) {
        PageHelper.startPage(page, pageSize);
        List<Appointment> list = appointmentMapper.findPage(status, keyword);
        PageInfo<Appointment> pageInfo = new PageInfo<>(list);
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageInfo.getList());
        result.put("total", pageInfo.getTotal());
        result.put("page", pageInfo.getPageNum());
        result.put("pageSize", pageInfo.getPageSize());
        return result;
    }

    @Override
    public void review(Long id, AppointmentReviewDTO dto, Long reviewerId) {
        Appointment appointment = appointmentMapper.findById(id);
        if (appointment == null) {
            throw new RuntimeException(msg("appointment.not_found"));
        }
        if (appointment.getStatus() != 0) {
            throw new RuntimeException(msg("appointment.status.invalid"));
        }
        if (dto.getStatus() == 2 && (dto.getRejectReason() == null || dto.getRejectReason().trim().isEmpty())) {
            throw new IllegalArgumentException(msg("appointment.reject.reason_required"));
        }
        appointmentMapper.updateStatus(id, dto.getStatus(), dto.getRejectReason(),
                reviewerId, LocalDateTime.now());
    }
}
