package org.timekeeper.configuration.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "urlscan")
public class UrlScanClientConfig {

    private String url;

    private String apiKey;

    private String apiKeyHeader;

}
