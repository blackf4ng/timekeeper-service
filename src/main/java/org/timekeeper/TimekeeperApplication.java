package org.timekeeper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

@Slf4j
@SpringBootApplication
@EnableJpaRepositories(basePackages = "org.timekeeper.database.postgresql.repository")
public class TimekeeperApplication {

    private static final String APPLICATION_ENV_VAR = "APPLICATION";

    private enum Application {
        API_SERVER,
        STATUS_POLLER,
        SCAN_REQUESTER
    }

    public static void main(String[] args) {
        Application application = Optional.of(System.getenv(APPLICATION_ENV_VAR))
            .map(Application::valueOf)
            .orElse(Application.API_SERVER);

		log.info("Starting application: application={}", application);
		switch (application) {
			case STATUS_POLLER, SCAN_REQUESTER:
				new SpringApplicationBuilder(TimekeeperApplication.class)
					.web(WebApplicationType.NONE)
					.build();
				break;
			case API_SERVER:
				SpringApplication.run(TimekeeperApplication.class, args);
				break;
			default:
				throw new IllegalArgumentException(
					String.format("Unknown application; unable to start: application=%s", application)
				);
		}
    }

}
