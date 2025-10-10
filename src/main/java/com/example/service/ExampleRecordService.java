package com.example.service;

import com.example.component.DataSourceUrlLogger;
import com.example.model.ExampleRecord;
import com.example.repository.ExampleRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ExampleRecordService {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceUrlLogger.class);

    @Autowired
    private ExampleRecordRepository repository;
    
    private final AtomicBoolean writeEnabled = new AtomicBoolean(false);
    private final AtomicBoolean scheduledTasksEnabled = new AtomicBoolean(true);
    private int lastRecordCount = -1;
    
    public List<ExampleRecord> getAllRecords() {
        return repository.findAll();
    }
    
    public ExampleRecord saveRecord(ExampleRecord record) {
        return repository.save(record);
    }
    
    public void enableWriting() {
        writeEnabled.set(true);
    }
    
    public void disableWriting() {
        writeEnabled.set(false);
    }
    
    public boolean toggleWriting() {
        boolean newStatus = !writeEnabled.get();
        writeEnabled.set(newStatus);
        return newStatus;
    }
    
    public boolean isWriteEnabled() {
        return writeEnabled.get();
    }
    
    public void disableScheduledTasks() {
        scheduledTasksEnabled.set(false);
        logger.info("Scheduled tasks disabled");
    }
    
    public void enableScheduledTasks() {
        scheduledTasksEnabled.set(true);
        logger.info("Scheduled tasks enabled");
    }
    
    @PostConstruct
    public void initializeTable() {
        // This method ensures the table is created if it doesn't exist
        // The table will be created automatically by JPA when the first query is executed
        try {
            long count = repository.count(); // Simple query to trigger table creation
            logger.info("Table 'example_records' is ready");
            List<ExampleRecord> records = getAllRecords();
            logger.info("Records found at startup: {}", records.size());

        } catch (Exception e) {
            System.err.println("Error initializing table: " + e.getMessage());
        }
    }
    
    @Scheduled(fixedRate = 500)
    public void readRecords() {
        if (!scheduledTasksEnabled.get()) {
            return;
        }
        
        try {
            List<ExampleRecord> records = getAllRecords();
            int currentCount = records.size();
            if (currentCount != lastRecordCount) {
                System.out.println(currentCount + " records found");
                lastRecordCount = currentCount;
            }
        } catch (Exception e) {
            // Silently ignore errors when scheduled tasks are being shut down
            if (scheduledTasksEnabled.get()) {
                logger.warn("Error reading records: {}", e.getMessage());
            }
        }
    }
    
    @Scheduled(fixedRate = 1000)
    public void writeRecord() {
        if (!scheduledTasksEnabled.get()) {
            return;
        }
        
        try {
            if (writeEnabled.get()) {
                ExampleRecord record = new ExampleRecord(
                    "Record " + System.currentTimeMillis(),
                    "Value " + System.currentTimeMillis()
                );
                saveRecord(record);
                System.out.println("Wrote new record: " + record);
            }
        } catch (Exception e) {
            // Silently ignore errors when scheduled tasks are being shut down
            if (scheduledTasksEnabled.get()) {
                logger.warn("Error writing record: {}", e.getMessage());
            }
        }
    }
}
