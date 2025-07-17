package com.example.repository;

import com.example.model.ExampleRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExampleRecordRepository extends JpaRepository<ExampleRecord, Long> {
}
