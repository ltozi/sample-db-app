package com.example.controller;

import com.example.model.ExampleRecord;
import com.example.service.ExampleRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
public class ExampleRecordController {
    
    @Autowired
    private ExampleRecordService service;
    
    @GetMapping
    public ResponseEntity<List<ExampleRecord>> getAllRecords() {
        List<ExampleRecord> records = service.getAllRecords();
        return ResponseEntity.ok(records);
    }
    
    @PostMapping("/toggle-writing")
    public ResponseEntity<String> toggleWriting() {
        boolean newStatus = service.toggleWriting();
        String message = newStatus ? "Writing enabled" : "Writing disabled";
        return ResponseEntity.ok(message);
    }
    
    @GetMapping("/writing-status")
    public ResponseEntity<Boolean> getWritingStatus() {
        return ResponseEntity.ok(service.isWriteEnabled());
    }
}
