package org.timekeeper.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.timekeeper.client.UrlScanClient;
import org.timekeeper.submitter.ScanSubmitter;
import org.timekeeper.poller.StatusPoller;
import org.timekeeper.service.ScanService;

import java.time.Clock;

@Configuration
public class PollerConfig {

    @Bean
    public ScanSubmitter.Delay scanSubmitterDelay() {
        return ScanSubmitter.Delay.builder().build();
    }

    @Bean
    public ScanSubmitter scanRequester(
        ScanService scanService,
        UrlScanClient urlScanClient,
        ScanSubmitter.Delay scanSubmitterDelay,
        Clock clock
    ) {
        return new ScanSubmitter(scanService, urlScanClient, scanSubmitterDelay, clock);
    }

    @Bean
    public StatusPoller.Delay statusPollerDelay() {
        return StatusPoller.Delay.builder().build();
    }

    @Bean
    public StatusPoller statusPoller(
        ScanService scanService,
        UrlScanClient urlScanClient,
        StatusPoller.Delay statusPollerDelay,
        Clock clock
    ) {
        return new StatusPoller(scanService, urlScanClient, statusPollerDelay, clock);
    }

}
