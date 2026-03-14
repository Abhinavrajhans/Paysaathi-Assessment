package com.example.PaySathi.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class WireMockConfig {

    private final WireMockServer wireMockServer;

    public WireMockConfig() {
        wireMockServer = new WireMockServer(
                WireMockConfiguration.options()
                        .port(8089)
                        .usingFilesUnderClasspath("wiremock")
        );
        wireMockServer.start();
        System.out.println("✅ WireMock server started on port 8089");
    }

    @PreDestroy
    public void stop() {
        if (wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }
}