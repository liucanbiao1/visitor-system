package com.visitor.dto;

import javax.validation.constraints.NotNull;

public class AppointmentReviewDTO {

    @NotNull(message = "审核状态不能为空")
    private Integer status;

    private String rejectReason;

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}
