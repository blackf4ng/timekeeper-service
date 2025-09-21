package org.timekeeper.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.timekeeper.client.UrlScanClient;
import org.timekeeper.poller.ScanRequester;
import org.timekeeper.poller.StatusPoller;
import org.timekeeper.service.ScanService;

import java.time.Clock;

@Configuration
public class PollerConfig {

    @Bean
    public ScanRequester scanRequester(
        ScanService scanService,
        UrlScanClient urlScanClient,
        Clock clock
    ) {
        return new ScanRequester(scanService, urlScanClient, clock);
    }

    @Bean
    public StatusPoller statusPoller(
        ScanService scanService,
        UrlScanClient urlScanClient,
        Clock clock
    ) {
        return new StatusPoller(scanService, urlScanClient, clock);
    }

}
