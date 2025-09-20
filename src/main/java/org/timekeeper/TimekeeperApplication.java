package org.timekeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.timekeeper.configuration.ClientConfiguration;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "org.timekeeper.database.postgresql.repository")
@EnableConfigurationProperties(ClientConfiguration.class)
public class TimekeeperApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimekeeperApplication.class, args);
	}

}
