package org.timekeeper.configuration.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.timekeeper.poller.StatusPoller;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Configures the status poller to run on a regular interval (currently 10 seconds)
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "application", name = "name", havingValue = "STATUS_POLLER")
public class StatusPollerConfig implements SchedulingConfigurer {

    @Autowired
    private StatusPoller statusPoller;

    @Autowired
    private Clock clock;

    @Bean
    public Executor taskExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
        taskRegistrar.addTriggerTask(
            statusPoller::poll,
            triggerContext -> Optional.ofNullable(triggerContext.lastActualExecution())
                .map(time -> time.plus(10, ChronoUnit.SECONDS))
                .orElseGet(() -> clock.instant())
        );
    }

}
