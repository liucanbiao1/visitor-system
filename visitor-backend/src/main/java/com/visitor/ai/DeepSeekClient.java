package com.visitor.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visitor.config.AiProperties;
import com.visitor.dto.ai.AiDecision;
import com.visitor.dto.ai.ChatCompletionRequest;
import com.visitor.dto.ai.ChatCompletionResponse;
import com.visitor.dto.ai.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class DeepSeekClient {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AiProperties props;

    public DeepSeekClient(RestTemplate aiRestTemplate, ObjectMapper objectMapper, AiProperties props) {
        this.restTemplate = aiRestTemplate;
        this.objectMapper = objectMapper;
        this.props = props;
    }

    public AiDecision review(String systemPrompt, String userPrompt) {
        ChatCompletionRequest req = buildRequest(systemPrompt, userPrompt);
        int maxAttempts = Math.max(1, props.getDeepseek().getMaxAttempts());

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                long start = System.currentTimeMillis();
                String content = doPost(req);
                AiDecision decision = parseContent(content);
                decision.setLatencyMs((int) (System.currentTimeMillis() - start));
                return decision;
            } catch (HttpStatusCodeException e) {
                if (isRetryable(e.getStatusCode().value()) && attempt < maxAttempts) {
                    log.warn("DeepSeek retryable status {} on attempt {}", e.getStatusCode().value(), attempt);
                    backoff(attempt);
                } else {
                    throw new DeepSeekException(extractErrorMessage(e), e);
                }
            } catch (ResourceAccessException e) {
                if (attempt < maxAttempts) {
                    log.warn("DeepSeek network/timeout on attempt {}: {}", attempt, e.getMessage());
                    backoff(attempt);
                } else {
                    throw new DeepSeekException("timeout_or_network: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                if (attempt < maxAttempts) {
                    log.warn("DeepSeek parse/unknown error on attempt {}: {}", attempt, e.getMessage());
                    backoff(attempt);
                } else {
                    throw new DeepSeekException("parse_error: " + e.getMessage(), e);
                }
            }
        }
        throw new DeepSeekException("retry_exhausted");
    }

    private ChatCompletionRequest buildRequest(String systemPrompt, String userPrompt) {
        ChatCompletionRequest req = new ChatCompletionRequest();
        AiProperties.DeepSeek ds = props.getDeepseek();
        req.setModel(ds.getModel());
        req.setMessages(Arrays.asList(
                new ChatMessage("system", systemPrompt),
                new ChatMessage("user", userPrompt)
        ));
        req.setTemperature(ds.getTemperature());
        req.setMaxTokens(ds.getMaxTokens());
        Map<String, String> responseFormat = new HashMap<>();
        responseFormat.put("type", "json_object");
        req.setResponseFormat(responseFormat);
        return req;
    }

    private String doPost(ChatCompletionRequest req) {
        AiProperties.DeepSeek ds = props.getDeepseek();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + ds.getApiKey());
        HttpEntity<ChatCompletionRequest> entity = new HttpEntity<>(req, headers);

        String url = ds.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";
        ResponseEntity<ChatCompletionResponse> response = restTemplate.postForEntity(url, entity, ChatCompletionResponse.class);
        if (response.getBody() == null || response.getBody().getChoices() == null
                || response.getBody().getChoices().isEmpty()) {
            throw new DeepSeekException("empty_response");
        }
        return response.getBody().getChoices().get(0).getMessage().getContent();
    }

    private AiDecision parseContent(String content) throws Exception {
        if (content == null || content.trim().isEmpty()) {
            throw new DeepSeekException("empty_content");
        }
        String cleaned = content.trim();
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline > 0) {
                cleaned = cleaned.substring(firstNewline + 1);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
        }
        AiDecision decision = objectMapper.readValue(cleaned, AiDecision.class);
        if (decision.getDecision() == null) {
            throw new DeepSeekException("missing_decision");
        }
        if (decision.getDecision() == 2 && (decision.getRejectReason() == null || decision.getRejectReason().trim().isEmpty())) {
            throw new DeepSeekException("missing_reject_reason");
        }
        return decision;
    }

    private boolean isRetryable(int status) {
        return status == 429 || status >= 500;
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(500L * attempt);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private String extractErrorMessage(HttpStatusCodeException e) {
        try {
            Map<?, ?> errorBody = objectMapper.readValue(e.getResponseBodyAsString(), Map.class);
            Object message = errorBody.get("error");
            if (message instanceof Map) {
                Object msg = ((Map<?, ?>) message).get("message");
                if (msg != null) {
                    return "status_" + e.getStatusCode().value() + ": " + msg;
                }
            } else if (message instanceof String) {
                return "status_" + e.getStatusCode().value() + ": " + message;
            }
        } catch (Exception ignored) {
            // fall through to generic message
        }
        return "status_" + e.getStatusCode().value();
    }

    public String getModelName() {
        return props.getDeepseek().getModel();
    }

    public boolean isApiKeyConfigured() {
        String key = props.getDeepseek().getApiKey();
        return key != null && !key.trim().isEmpty();
    }

    public String maskApiKey() {
        String key = props.getDeepseek().getApiKey();
        if (key == null || key.isEmpty()) return "";
        if (key.length() <= 8) return "****";
        return key.substring(0, 3) + "****" + key.substring(key.length() - 4);
    }
}
