package org.timekeeper.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.timekeeper.client.NoOpResponseErrorHandler;
import org.timekeeper.client.UrlScanClient;
import org.timekeeper.configuration.client.UrlScanClientConfig;

@Configuration
public class ClientConfig {

    @Bean
    public NoOpResponseErrorHandler noOpResponseErrorHandler() {
        return new NoOpResponseErrorHandler();
    }

    @Bean
    public UrlScanClient urlScanClient(UrlScanClientConfig clientConfig, NoOpResponseErrorHandler noOpResponseErrorHandler) {
        return new UrlScanClient(clientConfig, noOpResponseErrorHandler);
    }

}
