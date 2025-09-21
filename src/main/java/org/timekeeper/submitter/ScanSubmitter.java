package org.timekeeper.submitter;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.timekeeper.client.UrlScanClient;
import org.timekeeper.client.model.SubmitScanResponse;
import org.timekeeper.model.Page;
import org.timekeeper.model.ScanResult;
import org.timekeeper.model.ScanResultStatus;
import org.timekeeper.model.request.PageRequest;
import org.timekeeper.service.ScanService;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScanSubmitter {

    /**
     * Class for representing a delay to a specific time.
     * Synchronization is required on the setter in case multiple threads run the same process function at the same time
     */
    @Getter
    @ToString
    @Builder(toBuilder = true)
    public static class Delay {

        @Builder.Default
        private Optional<Instant> time = Optional.empty();

        @Synchronized
        public void setTime(Instant newTime) {
            if (
                time.filter(existingTime -> existingTime.isAfter(newTime))
                    .isPresent()
            ) {
                return;
            }

            time = Optional.of(newTime);
        }

    }

    protected static final ScanResultStatus STATUS = ScanResultStatus.SUBMITTED;

    protected static final Integer PAGE_SIZE = 20;

    private final ScanService scanService;

    private final UrlScanClient urlScanClient;

    private final Delay delay;

    private final Clock clock;

    public void submit() {
        Instant now = clock.instant();
        Optional<Instant> delayTime = delay.getTime();
        log.info("Polling for scan results to submit: status={} now={}", STATUS, now);
        if (
            delayTime.map(time -> time.isAfter(now))
                .orElse(false)
        ) {
            log.info("Requested delay has not been met; cancelling: delay={} now={}", delayTime, now);

            return;
        }

        Integer totalPages;
        Integer page = 0;
        do {
            PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .pageSize(PAGE_SIZE)
                .build();
            Page<ScanResult> scanResultPage = scanService.listScanResults(STATUS, pageRequest);
            List<ScanResult> scanResultList = scanResultPage.getData();
            log.info("Retrieved scan results for requesting scans: scanResultList={}", scanResultList);
            for (ScanResult scanResult : scanResultList) {
                Optional<Instant> delayUntil = process(scanResult);
                if (delayUntil.isPresent()) {
                    delay.setTime(delayUntil.get());
                    return;
                }
            }

            totalPages = scanResultPage.getTotalPages();
            page++;
        } while (page < totalPages);
    }

    private Optional<Instant> process(ScanResult scanResult) {
        ResponseEntity<SubmitScanResponse> responseEntity = urlScanClient.submitScan(scanResult.getUrl());
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        SubmitScanResponse response = responseEntity.getBody();

        if (HttpStatus.OK.equals(statusCode)) {
            scanService.updateScanResult(
                scanResult.getId(),
                ScanResultStatus.PROCESSING,
                Optional.empty(),
                Optional.ofNullable(response.getUuid()),
                Optional.ofNullable(response.getResult())
            );

            return Optional.empty();
        }

        if (HttpStatus.TOO_MANY_REQUESTS.equals(statusCode)) {
            Optional<Instant> resetTime = urlScanClient.getThrottleReset(responseEntity);
            log.info("Throttled by client; pausing until throttle window resets: resetTime={}", resetTime);

            return resetTime;
        }

        if (statusCode.is4xxClientError()) {
            scanService.updateScanResult(
                scanResult.getId(),
                ScanResultStatus.FAILED,
                Optional.of(
                    ScanResult.StatusDetails.builder()
                        .code(response.getStatus())
                        .message(response.getMessage())
                        .description(response.getDescription())
                        .build()
                ),
                Optional.empty(),
                Optional.empty()
            );

            return Optional.empty();
        }

        return Optional.empty();
    }

}
