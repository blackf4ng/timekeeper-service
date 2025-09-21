package org.timekeeper.configuration.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.timekeeper.submitter.ScanSubmitter;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "application", name = "name", havingValue = "SCAN_SUBMITTER")
public class ScanSubmitterConfig implements SchedulingConfigurer {

    @Autowired
    ScanSubmitter scanSubmitter;

    @Autowired
    Clock clock;

    @Bean
    public Executor taskExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
        taskRegistrar.addTriggerTask(
            scanSubmitter::submit,
            new Trigger() {
                @Override
                public Instant nextExecution(TriggerContext triggerContext) {
                    return Optional.ofNullable(triggerContext.lastActualExecution())
                        .map(time -> time.plus(10, ChronoUnit.SECONDS))
                        .orElseGet(() -> clock.instant());
                }
            }
        );
    }

}
