package org.timekeeper.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.timekeeper.database.postgresql.model.ScanEntity;
import org.timekeeper.database.postgresql.model.ScanResultEntity;
import org.timekeeper.database.postgresql.model.transform.PageRequestTransform;
import org.timekeeper.database.postgresql.repository.ScanRepository;
import org.timekeeper.database.postgresql.repository.ScanResultRepository;
import org.timekeeper.exception.DuplicateRequestException;
import org.timekeeper.exception.ResourceNotFoundException;
import org.timekeeper.model.Page;
import org.timekeeper.model.Scan;
import org.timekeeper.model.ScanResult;
import org.timekeeper.model.ScanResultStatus;
import org.timekeeper.model.ScanSummary;
import org.timekeeper.model.request.PageRequest;
import org.timekeeper.model.transform.PageTransform;
import org.timekeeper.model.transform.ScanResultTransform;
import org.timekeeper.model.transform.ScanSummaryTransform;
import org.timekeeper.model.transform.ScanTransform;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.timekeeper.database.postgresql.repository.Constants.DEFAULT_REVERSE_SORT;
import static org.timekeeper.database.postgresql.repository.Constants.DEFAULT_SORT;

/**
 * Service layer responsible for handling business logic related to scans
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScanService {

    protected static final Integer SCAN_DEDUPE_DAYS = 1;

    private final Clock clock;

    private final ScanRepository scanRepository;

    private final ScanResultRepository scanResultRepository;

    /**
     * Creates a scan for the given user with the requested URL.
     * Prevents the creation of multiple URL scans from the same user within the dedupe period
     * Reuses scan results for the same url across users within the dedupe period
     *
     * @param userId the ID of the user that is making the request
     * @param url    the URL that is requested to be scanned
     * @return the scan that was created
     * @throws DuplicateRequestException if a scan of the same url is requested by the same user within the dedupe window
     */
    @Transactional
    public Scan createScan(
        String userId,
        String url
    ) {
        Instant now = clock.instant();
        Instant dedupeCutoff = now.minus(SCAN_DEDUPE_DAYS, ChronoUnit.DAYS);
        log.info("Creating scan: userId={} url={} dedupeCutoff={}", userId, url, dedupeCutoff);
        // If the user has already submitted a scan request with the same URL, throw a duplicate request exception
        scanRepository.findFirstByUserIdAndResult_UrlAndCreatedAtAfter(userId, url, dedupeCutoff, DEFAULT_SORT)
            .ifPresent(scan -> {
                throw new DuplicateRequestException(
                    String.format(
                        "Duplicate scan request scan found: url=%s scanId=%s", url, scan.getId()
                    )
                );
            });
        // If there is a scan result that was submitted within the dedupe window across users, use that result
        Optional<ScanResultEntity> scanResultEntityOptional = scanResultRepository
            .findFirstByUrlAndCreatedAtAfter(url, dedupeCutoff, DEFAULT_SORT);
        if (scanResultEntityOptional.isPresent()) {
            ScanResultEntity scanResultEntity = scanResultEntityOptional.get();
            log.info("Existing scan result within deduplication window was found; using instead of creating new result: result={} dedupeCutoff={}", scanResultEntity, dedupeCutoff);

            ScanEntity scanEntity = scanRepository.save(
                ScanEntity.builder()
                    .userId(userId)
                    .result(scanResultEntity)
                    .build()
            );
            log.info("Successfully created scan: scanEntity={}", scanEntity);

            return ScanTransform.apply(scanEntity);
        }

        ScanEntity scanEntity = scanRepository.save(
            ScanEntity.builder()
                .userId(userId)
                .result(
                    ScanResultEntity.builder()
                        .url(url)
                        .build()
                ).build()
        );
        log.info("Successfully created scan: scanEntity={}", scanEntity);

        return ScanTransform.apply(scanEntity);
    }

    /**
     * Retrieves the details of a scan by the given scan ID
     *
     * @param userId The ID of the user that's making the request
     * @param scanId The ID of the scan that is to be retrieved
     * @return the scan details for the scan with the provided ID
     * @throws ResourceNotFoundException if either the user does not have access to the scan or the scan does not exist. In the interest of security,
     *                                   a single ResourceNotFoundException is thrown instead of distinguishing between the scan existing and the user not being authorized to view it
     */
    public Scan getScan(String userId, Long scanId) {
        log.info("Retrieving scan: userId={} scanId={}", userId, scanId);

        Optional<ScanEntity> scanEntityOptional = scanRepository.findByIdAndUserId(scanId, userId);
        log.info("Retrieved scan: userId={} scanId={} scanEntity={}", userId, scanId, scanEntityOptional);
        return scanEntityOptional
            .map(ScanTransform::apply)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    String.format("Scan not found: scanId=%s", scanId)
                )
            );
    }

    /**
     * Lists all scans for a given user sorted in descending order of when the scan was created at. Optionally filters the results by the status of the scan
     *
     * @param userId         The ID of the user that scans are to be retrieved for
     * @param statusOptional An optional status to filter the results for
     * @param pageRequest    the pagination scheme to use
     * @return A page of scans meeting the filter criteria sorted in descending order of when the scan was created
     */
    public Page<ScanSummary> listScanSummaries(
        String userId,
        Optional<ScanResultStatus> statusOptional,
        PageRequest pageRequest
    ) {
        log.info("Listing scan summaries: userId={} status={} pageRequest={}", userId, statusOptional, pageRequest);

        org.springframework.data.domain.PageRequest sortedPageRequest = PageRequestTransform.apply(pageRequest)
            .withSort(DEFAULT_SORT);

        org.springframework.data.domain.Page<ScanEntity> scanEntityPage = statusOptional
            .map(status -> scanRepository.findAllByUserIdAndResult_Status(userId, status, sortedPageRequest))
            .orElseGet(() -> scanRepository.findAllByUserId(userId, sortedPageRequest));

        log.info("Successfully listed scan summaries: userId={} status={} pageRequest={} scanEntityPage={}", userId, statusOptional, pageRequest, scanEntityPage);
        return PageTransform.apply(
            scanEntityPage,
            ScanSummaryTransform::apply
        );
    }

    /**
     * Deletes a scan for a specific user.
     * Users are not allowed to delete scans which are not created by them.
     * In order to maintain idempotency for multiple deletes of the same scan,
     * no exception will be thrown if the scan does not exist or if the user does not have access to the scan
     *
     * @param userId The ID of the requesting user
     * @param scanId The ID of the requested scan
     */
    public void deleteScan(String userId, Long scanId) {
        log.info("Deleting scan: userId={} scanId={}", userId, scanId);
        scanRepository.findByIdAndUserId(scanId, userId)
            .ifPresentOrElse(scan -> {
                    scanRepository.deleteById(scan.getId());

                    log.info("Successfully deleted scan: userId={} scanId={}", userId, scanId);
                },
                () -> log.info("Scan not found; ignoring: userId={} scanId={}", userId, scanId)
            );
    }

    /**
     * Updates a scan result with additional information.
     *
     * @param scanResultId ID of the scan result to update
     * @param status       the updated status of the scan result
     * @return The scan result after updates were applied
     * @throws ResourceNotFoundException if a scan result with the provided ID does not exist
     */
    @Transactional
    public ScanResult updateScanResult(
        Long scanResultId,
        ScanResultStatus status,
        Optional<ScanResult.StatusDetails> statusDetails,
        Optional<String> urlScanId,
        Optional<String> resultUrl
    ) {
        log.info("Updating scan result: scanResultId={} status={} urlScanId={} resultUrl={}", scanResultId, status, urlScanId, resultUrl);
        ScanResultEntity previousScanResultEntity = scanResultRepository.findById(scanResultId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    String.format("Scan result not found: scanResultId=%s", scanResultId)
                )
            );

        ScanResultEntity.ScanResultEntityBuilder newScanResultEntityBuilder = previousScanResultEntity.toBuilder();
        newScanResultEntityBuilder.status(status);
        urlScanId.ifPresent(newScanResultEntityBuilder::urlScanId);
        resultUrl.ifPresent(newScanResultEntityBuilder::resultUrl);
        statusDetails.ifPresent(details -> {
                newScanResultEntityBuilder.statusCode(details.getCode());
                newScanResultEntityBuilder.statusDescription(details.getDescription());
                newScanResultEntityBuilder.statusMessage(details.getMessage());
            }
        );

        ScanResultEntity newscanResultEntity = scanResultRepository.save(newScanResultEntityBuilder.build());
        log.info("Successfully updated scan result: scanResultId={} scanResultEntity={}", scanResultId, newscanResultEntity);
        return ScanResultTransform.apply(newscanResultEntity);
    }

    /**
     * Lists all scan results with a given status sorted in ascending order of when the scan result was created.
     *
     * @param status      the status of scan results to filter on
     * @param pageRequest the pagination scheme to use
     * @return A page of scan results meeting the filter criteria sorted in ascending order of when the scan result was created
     */
    public Page<ScanResult> listScanResults(
        ScanResultStatus status,
        PageRequest pageRequest
    ) {
        return PageTransform.apply(
            scanResultRepository.findAllByStatus(
                status,
                PageRequestTransform.apply(pageRequest).withSort(DEFAULT_REVERSE_SORT)
            ),
            ScanResultTransform::apply
        );
    }

}
