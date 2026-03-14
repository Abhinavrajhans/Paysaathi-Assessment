package com.example.PaySathi.config;

import com.example.PaySathi.services.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncOnStartup implements ApplicationRunner {

    private final SyncService syncService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Running initial sync on startup...");
        syncService.syncAll();
    }
}