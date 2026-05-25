package com.visitor.entity;

import java.time.LocalDateTime;

public class AccessLog {
    private Long id;
    private Long visitorId;
    private Long appointmentId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Integer accessStatus;
    private String deviceName;
    private String failReason;
    private LocalDateTime createTime;

    // display fields from JOIN
    private String visitorName;
    private String visitorPhone;

    public AccessLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVisitorId() { return visitorId; }
    public void setVisitorId(Long visitorId) { this.visitorId = visitorId; }
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }
    public Integer getAccessStatus() { return accessStatus; }
    public void setAccessStatus(Integer accessStatus) { this.accessStatus = accessStatus; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }
    public String getVisitorPhone() { return visitorPhone; }
    public void setVisitorPhone(String visitorPhone) { this.visitorPhone = visitorPhone; }
}
