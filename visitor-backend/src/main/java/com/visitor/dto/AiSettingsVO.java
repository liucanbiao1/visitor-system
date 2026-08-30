package com.visitor.dto;

public class AiSettingsVO {

    private Boolean enabled;
    private String model;
    private String baseUrl;
    private Boolean apiKeyConfigured;
    private String apiKeyMasked;

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public Boolean getApiKeyConfigured() { return apiKeyConfigured; }
    public void setApiKeyConfigured(Boolean apiKeyConfigured) { this.apiKeyConfigured = apiKeyConfigured; }
    public String getApiKeyMasked() { return apiKeyMasked; }
    public void setApiKeyMasked(String apiKeyMasked) { this.apiKeyMasked = apiKeyMasked; }
}
