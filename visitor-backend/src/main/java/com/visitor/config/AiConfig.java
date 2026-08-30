package com.visitor.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AiConfig {

    @Bean
    public RestTemplate aiRestTemplate(RestTemplateBuilder builder, AiProperties props) {
        AiProperties.DeepSeek ds = props.getDeepseek();
        return builder
                .setConnectTimeout(Duration.ofMillis(ds.getConnectTimeout()))
                .setReadTimeout(Duration.ofMillis(ds.getReadTimeout()))
                .build();
    }
}
