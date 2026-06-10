package com.example.graphql.client.config;

import com.example.graphql.client.CustomerShipmentGraphQlClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
public class GraphQlClientConfig {

    @Bean
    CustomerShipmentGraphQlClient customerShipmentGraphQlClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            Environment environment,
            @Value("${app.graphql.client.base-url:}") String configuredBaseUrl) {
        return new CustomerShipmentGraphQlClient(
                restClientBuilder,
                objectMapper,
                () -> resolveBaseUrl(configuredBaseUrl, environment));
    }

    static String resolveBaseUrl(String configuredBaseUrl, Environment environment) {
        if (StringUtils.hasText(configuredBaseUrl)) {
            return configuredBaseUrl;
        }
        String port = environment.getProperty("local.server.port", environment.getProperty("server.port", "8080"));
        return "http://127.0.0.1:" + port;
    }
}