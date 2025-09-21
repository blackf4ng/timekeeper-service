package org.timekeeper.poller;

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
import org.timekeeper.client.model.GetResultResponse;
import org.timekeeper.model.Page;
import org.timekeeper.model.ScanResult;
import org.timekeeper.model.ScanResultStatus;
import org.timekeeper.model.request.PageRequest;
import org.timekeeper.service.ScanService;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Polls the status of scans that are still processing and updates their status
 * if either processing is finished or an error is encountered
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatusPoller {

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

    protected static final ScanResultStatus STATUS = ScanResultStatus.PROCESSING;

    protected static final Integer PAGE_SIZE = 20;

    private final ScanService scanService;

    private final UrlScanClient urlScanClient;

    private final Delay delay;

    private final Clock clock;

    public void poll() {
        Instant now = clock.instant();
        Optional<Instant> delayTime = delay.getTime();
        log.info("Polling for scan results to update scan status: status={} now={}", STATUS, now);
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
            log.info("Retrieved scan results for checking scan status: scanResultList={}", scanResultList);
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
        ResponseEntity<GetResultResponse> responseEntity = urlScanClient.getResult(scanResult.getUrlScanId());
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        GetResultResponse response = responseEntity.getBody();

        // If the status was a 200, then the report is now ready
        if (HttpStatus.OK.equals(statusCode)) {
            scanService.updateScanResult(
                scanResult.getId(),
                ScanResultStatus.DONE,
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
            );

            return Optional.empty();
        }

        // If the request was throttled, return when the throttle count is set to reset
        if (HttpStatus.TOO_MANY_REQUESTS.equals(statusCode)) {
            Optional<Instant> resetTime = urlScanClient.getThrottleReset(responseEntity);
            log.info("Throttled by client; pausing until throttle window resets: resetTime={}", resetTime);

            return resetTime;
        }

        // If the result is still in progress, then wait to retry later
        if (urlScanClient.isInProgress(responseEntity)) {
            return Optional.empty();
        }

        // If it is a standard 4xx error, then the scanning has failed
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
