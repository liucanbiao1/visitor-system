package com.visitor.entity;

import java.time.LocalDateTime;

public class SysPermission {
    private Long id;
    private String permName;
    private String permCode;
    private String permPath;
    private Long parentId;
    private String permType;
    private Integer sortOrder;
    private String icon;
    private LocalDateTime createTime;

    public SysPermission() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPermName() { return permName; }
    public void setPermName(String permName) { this.permName = permName; }
    public String getPermCode() { return permCode; }
    public void setPermCode(String permCode) { this.permCode = permCode; }
    public String getPermPath() { return permPath; }
    public void setPermPath(String permPath) { this.permPath = permPath; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getPermType() { return permType; }
    public void setPermType(String permType) { this.permType = permType; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
