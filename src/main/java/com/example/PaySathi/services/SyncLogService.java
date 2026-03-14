package com.example.PaySathi.services;

import com.example.PaySathi.models.SyncLog;
import com.example.PaySathi.models.SyncStatus;
import com.example.PaySathi.repositories.SyncLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncLogService {

    private final SyncLogRepository syncLogRepository;

    public SyncLog startLog(String entityType) {
        SyncLog syncLog = SyncLog.builder()
                .entityType(entityType)
                .status(SyncStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();
        return syncLogRepository.save(syncLog);
    }

    public void completeLog(SyncLog syncLog, int fetched, int saved) {
        syncLog.setStatus(SyncStatus.SUCCESS);
        syncLog.setRecordsFetched(fetched);
        syncLog.setRecordsSaved(saved);
        syncLog.setCompletedAt(LocalDateTime.now());
        syncLogRepository.save(syncLog);
    }

    public void failLog(SyncLog syncLog, String errorMessage) {
        syncLog.setStatus(SyncStatus.FAILED);
        syncLog.setErrorMessage(errorMessage);
        syncLog.setCompletedAt(LocalDateTime.now());
        syncLogRepository.save(syncLog);
    }
}