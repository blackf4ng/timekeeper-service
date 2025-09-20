package org.timekeeper.configuration;

import lombok.Data;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Data
@Configuration
@ConfigurationProperties(prefix = "clients")
public class ClientConfiguration {

    private Set<String> internal;

}
