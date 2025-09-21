package org.timekeeper.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.timekeeper.client.UrlScanClient;
import org.timekeeper.configuration.client.UrlScanClientConfig;

@Configuration
public class ClientConfig {

    @Bean
    public UrlScanClient urlScanClient(UrlScanClientConfig clientConfig) {
        return new UrlScanClient(clientConfig);
    }

}
