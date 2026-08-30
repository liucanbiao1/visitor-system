package com.visitor.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ChatCompletionRequest {

    private String model;
    private List<ChatMessage> messages;
    private double temperature;
    @JsonProperty("max_tokens")
    private int maxTokens;
    @JsonProperty("response_format")
    private Map<String, String> responseFormat;

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    public Map<String, String> getResponseFormat() { return responseFormat; }
    public void setResponseFormat(Map<String, String> responseFormat) { this.responseFormat = responseFormat; }
}
