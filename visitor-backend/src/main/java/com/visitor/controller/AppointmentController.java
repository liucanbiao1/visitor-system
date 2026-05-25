package com.visitor.controller;

import com.visitor.annotation.RequirePermission;
import com.visitor.common.Result;
import com.visitor.dto.AppointmentReviewDTO;
import com.visitor.dto.AppointmentSubmitDTO;
import com.visitor.entity.Appointment;
import com.visitor.service.AppointmentService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final MessageSource messageSource;

    public AppointmentController(AppointmentService appointmentService, MessageSource messageSource) {
        this.appointmentService = appointmentService;
        this.messageSource = messageSource;
    }

    @PostMapping("/submit")
    public Result<Void> submit(@Valid @RequestBody AppointmentSubmitDTO dto) {
        appointmentService.submitAppointment(dto);
        String msg = messageSource.getMessage("appointment.submit.success", null,
                LocaleContextHolder.getLocale());
        return Result.success(msg);
    }

    @GetMapping("/query")
    public Result<List<Appointment>> query(@RequestParam String phone) {
        return Result.success(appointmentService.queryByPhone(phone));
    }

    @GetMapping("/list")
    @RequirePermission("appointment:list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(appointmentService.getPage(page, pageSize, status, keyword));
    }

    @PutMapping("/{id}/review")
    @RequirePermission("appointment:review")
    public Result<Void> review(@PathVariable Long id,
                               @Valid @RequestBody AppointmentReviewDTO dto,
                               HttpServletRequest request) {
        Long reviewerId = (Long) request.getAttribute("userId");
        appointmentService.review(id, dto, reviewerId);
        return Result.success();
    }
}
