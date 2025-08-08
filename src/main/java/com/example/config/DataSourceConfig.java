package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String originalUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${app.name}")
    private String applicationPropertiesValue;

    @Bean
    @Primary
    public DataSource dataSource() {
        String modifiedUrl = buildModifiedUrl(originalUrl, applicationPropertiesValue);
        
        return DataSourceBuilder.create()
                .url(modifiedUrl)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    private String buildModifiedUrl(String originalUrl, String appPropertiesValue) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Original datasource URL cannot be null or empty");
        }

        // Check if URL already has parameters
        String separator = originalUrl.contains("?") ? "&" : "?";
        String customParameter = "ApplicationName=" + appPropertiesValue;
        
        // Avoid duplicate parameters
        if (originalUrl.contains("ApplicationName=")) {
            // Replace existing ApplicationProperties parameter
            return originalUrl.replaceAll("ApplicationName=[^&]*", customParameter);
        } else {
            // Add new parameter
            return originalUrl + separator + customParameter;
        }
    }
}
