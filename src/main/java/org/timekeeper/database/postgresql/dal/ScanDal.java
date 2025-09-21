package org.timekeeper.database.postgresql.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.timekeeper.database.postgresql.model.Scan;
import org.timekeeper.database.postgresql.model.ScanResult;
import org.timekeeper.database.postgresql.repository.ScanRepository;
import org.timekeeper.model.ScanResultStatus;

import java.util.Optional;

import static org.timekeeper.database.postgresql.dal.Constants.DEFAULT_SORT;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScanDal {

    private final ScanRepository scanRepository;

    public Optional<Scan> getScanOptional(Long id) {
        return scanRepository.findById(id);
    }

    public Optional<Scan> getScanOptional(String userId, Long id) {
        return getScanOptional(id)
            .filter(scan -> userId.equals(scan.getUserId()));
    }

    public Optional<Scan> getLatestScanOptional(String userId, String url) {
        log.info("Retrieving latest scan: userId={} url={}", userId, url);

        final Optional<Scan> scanOptional = scanRepository.findFirstByUserIdAndResult_Url(userId, url, DEFAULT_SORT);
        log.info("Retrieved latest scan: scan={}", scanOptional);
        return scanOptional;
    }

    public Page<Scan> listScans(String userId, PageRequest pageRequest) {
        log.info("Listing scans: userId={} pageRequest={}", userId, pageRequest);

        Pageable pageable = pageRequest
            .withSort(DEFAULT_SORT);

        return scanRepository.findAllByUserId(userId, pageable);
    }

    public Page<Scan> listScans(
        String userId,
        ScanResultStatus status,
        PageRequest pageRequest
    ) {
        log.info("Listing scans: userId={} status={} pageRequest={}", userId, status, pageRequest);

        Pageable pageable = pageRequest
            .withSort(DEFAULT_SORT);

        return scanRepository.findAllByUserIdAndResult_Status(userId, status, pageable);
    }

    public Scan createScan(
        String userId,
        String url
    ) {
        log.info("Creating scan: userId={} url={}", userId, url);

        return scanRepository.save(
            org.timekeeper.database.postgresql.model.Scan.builder()
                .userId(userId)
                .result(
                    ScanResult.builder()
                        .url(url)
                        .build()
                ).build()
        );
    }

    public Scan createScan(
        String userId,
        ScanResult scanResult
    ) {
        return scanRepository.save(
            org.timekeeper.database.postgresql.model.Scan.builder()
                .userId(userId)
                .result(scanResult)
                .build()
        );
    }

    public void deleteScan(Long scanId) {
        scanRepository.deleteById(scanId);
    }

}
