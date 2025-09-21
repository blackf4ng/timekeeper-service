package org.timekeeper.database.postgresql.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.timekeeper.database.postgresql.model.ScanResult;
import org.timekeeper.database.postgresql.repository.ScanResultRepository;
import org.timekeeper.exception.ResourceNotFoundException;
import org.timekeeper.model.ScanResultStatus;

import java.util.Optional;

import static org.timekeeper.database.postgresql.dal.Constants.DEFAULT_SORT;

@Component
@RequiredArgsConstructor
public class ScanResultDal {

    private final ScanResultRepository repository;

    public Optional<ScanResult> getScanResultOptional(Long scanResultId) {
        return repository.findById(scanResultId);
    }

    public Optional<ScanResult> getLatestScanResultOptional(String url) {
        return repository.findFirstByUrl(url, DEFAULT_SORT);
    }

    public ScanResult updateScanResult(
        ScanResult result,
        ScanResultStatus status,
        Optional<org.timekeeper.model.ScanResult.StatusDetails> statusDetails,
        Optional<String> urlScanId,
        Optional<String> resultUrl
    ) {
        org.timekeeper.database.postgresql.model.ScanResult.ScanResultBuilder resultBuilder = repository.findById(result.getId())
            .map(org.timekeeper.database.postgresql.model.ScanResult::toBuilder)
            .orElseThrow(() -> new ResourceNotFoundException(
                    String.format("Scan result not found: id=%s", result.getId())
                )
            );

        resultBuilder.status(status);
        urlScanId.ifPresent(resultBuilder::urlScanId);
        resultUrl.ifPresent(resultBuilder::resultUrl);
        statusDetails.ifPresent(details -> {
                resultBuilder.statusCode(details.getCode());
                resultBuilder.statusDescription(details.getDescription());
                resultBuilder.statusMessage(details.getMessage());
            }
        );

        return repository.save(
                resultBuilder.build()
            );
    }

    public Page<ScanResult> listScanResults(
        ScanResultStatus status,
        PageRequest pageRequest
    ) {
        Pageable pageable = pageRequest
            .withSort(DEFAULT_SORT);

        return repository.findAllByStatus(status, pageable);
    }

}
