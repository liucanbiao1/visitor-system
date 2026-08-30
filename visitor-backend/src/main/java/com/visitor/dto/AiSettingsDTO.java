package com.visitor.dto;

import javax.validation.constraints.NotNull;

public class AiSettingsDTO {

    @NotNull(message = "enabled cannot be null")
    private Boolean enabled;

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
