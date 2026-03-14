package com.example.PaySathi.repositories;

import com.example.PaySathi.models.SyncLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncLogRepository extends JpaRepository<SyncLog,Long> {
}
