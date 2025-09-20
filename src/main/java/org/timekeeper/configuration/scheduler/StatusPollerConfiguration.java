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
import org.timekeeper.poller.ScanStatusPoller;
import org.timekeeper.service.ScanService;

import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
@ConditionalOnProperty(value = "application", havingValue = "STATUS_POLLER")
public class StatusPollerConfiguration implements SchedulingConfigurer {

    @Autowired
    private ScanService scanService;

    @Bean
    public Executor taskExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ScanStatusPoller scanStatusPoller = new ScanStatusPoller(scanService);

        taskRegistrar.setScheduler(taskExecutor());
        taskRegistrar.addTriggerTask(
            scanStatusPoller::poll,
            new Trigger() {
                @Override
                public Instant nextExecution(TriggerContext triggerContext) {
                    return null;
                }
            }
        );
    }

}
