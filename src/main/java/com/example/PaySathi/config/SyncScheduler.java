package com.example.PaySathi.config;


import com.example.PaySathi.services.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncScheduler {

    private final SyncService syncService;

    // Runs every 30 minutes after the previous run completes
    // fixedDelay means: wait 30 min AFTER last run finishes
    // fixedRate would mean: run every 30 min regardless of how long it takes
    // fixedDelay is safer for sync jobs — prevents overlapping runs
    @Scheduled(fixedDelayString = "${sync.interval.ms}")
    public void scheduledSync() {
        log.info("Scheduler triggered — starting periodic sync...");
        syncService.syncAll();
    }
}