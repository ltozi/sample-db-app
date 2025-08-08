package com.example.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Method;

@Component
public class DataSourceUrlLogger implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceUrlLogger.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Try to extract URL from the datasource using reflection
            String url = extractUrlFromDataSource(dataSource);
            logger.info("Datasource URL configured with custom parameter: {}", url);
        } catch (Exception e) {
            logger.warn("Could not extract URL from datasource for logging: {}", e.getMessage());
        }
    }

    private String extractUrlFromDataSource(DataSource dataSource) throws Exception {
        // Try common methods to get URL from different DataSource implementations
        String[] methodNames = {"getJdbcUrl", "getUrl", "getURL"};
        
        for (String methodName : methodNames) {
            try {
                Method method = dataSource.getClass().getMethod(methodName);
                Object result = method.invoke(dataSource);
                if (result instanceof String) {
                    return (String) result;
                }
            } catch (Exception e) {
                // Try next method
                continue;
            }
        }
        
        // If direct methods don't work, try to get from connection
        try (var connection = dataSource.getConnection()) {
            return connection.getMetaData().getURL();
        }
    }
}
