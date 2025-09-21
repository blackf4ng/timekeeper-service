package org.timekeeper.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.timekeeper.database.postgresql.dal.ScanDal;
import org.timekeeper.database.postgresql.dal.ScanResultDal;
import org.timekeeper.database.postgresql.model.transform.PageRequestTransform;
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

/**
 * Service layer responsible for handling business logic related to scans
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScanService {

    private static final Integer SCAN_DEDUPE_DAYS = 1;

    private final Clock clock;

    private final ScanDal scanDal;

    private final ScanResultDal scanResultDal;

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
        // If the user has already submitted a scan request with the same URL, throw a duplicate request exception
        scanDal.getLatestScanOptional(userId, url)
            .filter(scan -> dedupeCutoff.isBefore(scan.getCreatedAt()))
            .ifPresent(scan -> {
                throw new DuplicateRequestException(
                    String.format(
                        "Duplicate scan request scan found: url=%s scanId=%s", url, scan.getId()
                    )
                );
            });
        // If there was a scan that was submitted within the dedupe window across users, use that result
        Optional<org.timekeeper.database.postgresql.model.ScanResult> scanResultOptional = scanResultDal.getLatestScanResultOptional(url)
            .filter(result -> dedupeCutoff.isBefore(result.getCreatedAt()));
        if (scanResultOptional.isPresent()) {
            org.timekeeper.database.postgresql.model.ScanResult scanResult = scanResultOptional.get();
            log.info("Existing scan result within deduplication window was found; using instead : result={} dedupeCutoff={}", scanResult, dedupeCutoff);

            return ScanTransform.apply(scanDal.createScan(userId, scanResult));
        }

        return ScanTransform.apply(scanDal.createScan(userId, url));
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
    public Scan getScan(
        String userId,
        Long scanId
    ) {
        return scanDal.getScanOptional(scanId)
            .filter(scan -> userId.equals(scan.getUserId()))
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
     * @return A paginated list of scans meeting the filter criteria sorted in descending order of when the scan was created
     */
    public Page<ScanSummary> listScanSummaries(
        String userId,
        Optional<ScanResultStatus> statusOptional,
        PageRequest pageRequest
    ) {
        return PageTransform.apply(
            statusOptional
                .map(status -> scanDal.listScans(userId, status, PageRequestTransform.apply(pageRequest)))
                .orElseGet(() -> scanDal.listScans(userId, PageRequestTransform.apply(pageRequest))),
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
        scanDal.getScanOptional(userId, scanId)
            .ifPresent(scan -> scanDal.deleteScan(scan.getId()));
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
        org.timekeeper.database.postgresql.model.ScanResult scanResult = scanResultDal.getScanResultOptional(scanResultId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    String.format("Scan result not found: scanResultId=%s", scanResultId)
                )
            );

        return ScanResultTransform.apply(
            scanResultDal.updateScanResult(scanResult, status, statusDetails, urlScanId, resultUrl)
        );
    }

    public Page<ScanResult> listScanResults(
        ScanResultStatus status,
        PageRequest pageRequest
    ) {
        return PageTransform.apply(
            scanResultDal.listScanResults(status, PageRequestTransform.apply(pageRequest)),
            ScanResultTransform::apply
        );
    }

}
