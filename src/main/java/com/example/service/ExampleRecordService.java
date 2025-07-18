package com.example.service;

import com.example.model.ExampleRecord;
import com.example.repository.ExampleRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ExampleRecordService {
    
    @Autowired
    private ExampleRecordRepository repository;
    
    private final AtomicBoolean writeEnabled = new AtomicBoolean(false);
    
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
    
    @PostConstruct
    public void initializeTable() {
        // This method ensures the table is created if it doesn't exist
        // The table will be created automatically by JPA when the first query is executed
        try {
            repository.count(); // Simple query to trigger table creation
            System.out.println("Table 'example_records' is ready. Current record count: " + repository.count());
        } catch (Exception e) {
            System.err.println("Error initializing table: " + e.getMessage());
        }
    }
    
    @Scheduled(fixedRate = 3000) // Every 3 seconds
    public void readRecords() {
        List<ExampleRecord> records = getAllRecords();
        System.out.println("Reading " + records.size() + " records from database at " + LocalDateTime.now());
       //records.forEach(System.out::println);
    }
    
    @Scheduled(fixedRate = 5000) // Every 10 seconds
    public void writeRecord() {
        if (writeEnabled.get()) {
            ExampleRecord record = new ExampleRecord(
                "Record " + System.currentTimeMillis(),
                "Value " + System.currentTimeMillis()
            );
            saveRecord(record);
            System.out.println("Wrote new record: " + record);
        } else {
            System.out.println("Writing is disabled at " + LocalDateTime.now());
        }
    }
}
