package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

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
    
    // HikariCP configuration properties
    @Value("${spring.datasource.hikari.connection-test-query:SELECT 1}")
    private String connectionTestQuery;
    
    @Value("${spring.datasource.hikari.validation-timeout:3000}")
    private long validationTimeout;
    
    @Value("${spring.datasource.hikari.connection-timeout:20000}")
    private long connectionTimeout;
    
    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long maxLifetime;
    
    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;

    @Bean
    @Primary
    public DataSource dataSource() {
        String modifiedUrl = buildModifiedUrl(originalUrl, applicationPropertiesValue);
        
        // Create HikariConfig and set properties
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(modifiedUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Apply HikariCP-specific properties
        config.setConnectionTestQuery(connectionTestQuery);
        config.setValidationTimeout(validationTimeout);
        config.setConnectionTimeout(connectionTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setIdleTimeout(idleTimeout);
        
        // Additional recommended settings for blue-green deployments
        config.setLeakDetectionThreshold(60000); // 1 minute
        config.setPoolName(applicationPropertiesValue + "-HikariPool");
        
        // Force connection testing to see SELECT 1 queries (for testing)
        // config.addDataSourceProperty("testOnBorrow", "true");
        // config.addDataSourceProperty("testOnReturn", "true");
        
        return new HikariDataSource(config);
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
