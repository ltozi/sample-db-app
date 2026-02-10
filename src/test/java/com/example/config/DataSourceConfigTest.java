package com.example.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataSourceConfigTest {

    @Test
    void triggerCacheHit() {

        System.out.println("Trigger cache hit (2)");
        System.out.println("Trigger cache hit (3)");

    }


    @Test
    void testUrlBuildingLogic() {
        DataSourceConfig config = new DataSourceConfig();
        
        // Test URL without existing parameters
        String originalUrl = "jdbc:postgresql://localhost:5432/testdb";
        String result = invokePrivateMethod(config, originalUrl, "myapp");
        assertEquals("jdbc:postgresql://localhost:5432/testdb?ApplicationName=myapp", result);
        
        // Test URL with existing parameters
        originalUrl = "jdbc:postgresql://localhost:5432/testdb?ssl=true";
        result = invokePrivateMethod(config, originalUrl, "myapp");
        assertEquals("jdbc:postgresql://localhost:5432/testdb?ssl=true&ApplicationName=myapp", result);
        
        // Test URL with existing ApplicationName parameter
        originalUrl = "jdbc:postgresql://localhost:5432/testdb?ApplicationName=oldvalue&ssl=true";
        result = invokePrivateMethod(config, originalUrl, "newvalue");
        assertEquals("jdbc:postgresql://localhost:5432/testdb?ApplicationName=newvalue&ssl=true", result);
        
        // Test edge cases
        originalUrl = "jdbc:postgresql://localhost:5432/testdb?ApplicationName=oldvalue";
        result = invokePrivateMethod(config, originalUrl, "newvalue");
        assertEquals("jdbc:postgresql://localhost:5432/testdb?ApplicationName=newvalue", result);
    }
    
    @Test
    void testNullAndEmptyUrls() {
        DataSourceConfig config = new DataSourceConfig();
        
        // Test null URL
        assertThrows(IllegalArgumentException.class, () -> 
            invokePrivateMethod(config, null, "myapp"));
        
        // Test empty URL
        assertThrows(IllegalArgumentException.class, () -> 
            invokePrivateMethod(config, "", "myapp"));
        
        // Test whitespace URL
        assertThrows(IllegalArgumentException.class, () -> 
            invokePrivateMethod(config, "   ", "myapp"));
    }
    
    private String invokePrivateMethod(DataSourceConfig config, String originalUrl, String appPropertiesValue) {
        try {
            java.lang.reflect.Method method = DataSourceConfig.class.getDeclaredMethod("buildModifiedUrl", String.class, String.class);
            method.setAccessible(true);
            return (String) method.invoke(config, originalUrl, appPropertiesValue);
        } catch (Exception e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e.getCause();
            }
            throw new RuntimeException("Failed to invoke private method", e);
        }
    }
}
