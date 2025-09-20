package org.timekeeper.database.postgresql.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.timekeeper.database.postgresql.model.transform.PageRequestTransform;
import org.timekeeper.database.postgresql.repository.ScanRepository;
import org.timekeeper.model.Page;
import org.timekeeper.model.Scan;
import org.timekeeper.model.ScanResult;
import org.timekeeper.model.ScanResultStatus;
import org.timekeeper.model.request.PageRequest;
import org.timekeeper.model.transform.PageTransform;
import org.timekeeper.model.transform.ScanTransform;

import java.util.Optional;

import static org.timekeeper.database.postgresql.dal.Constants.DEFAULT_SORT;

@Component
@RequiredArgsConstructor
public class ScanDal {

    private final ScanRepository repository;

    public Optional<Scan> getScanOptional(Long id) {
        return repository.findById(id)
            .map(ScanTransform::apply);
    }

    public Optional<Scan> getLatestScanOptional(String url) {
        return repository.findFirstByResult_Url(url, DEFAULT_SORT)
            .map(ScanTransform::apply);
    }

    public org.timekeeper.model.Page<Scan> listScans(String userId, PageRequest pageRequest) {
        Pageable pageable = PageRequestTransform.apply(pageRequest)
            .withSort(DEFAULT_SORT);

        return PageTransform.apply(
            repository.findAllByUserId(userId, pageable),
            ScanTransform::apply
        );
    }

    public Page<Scan> listScans(
        String userId,
        ScanResultStatus status,
        PageRequest pageRequest
    ) {
        Pageable pageable = PageRequestTransform.apply(pageRequest)
            .withSort(DEFAULT_SORT);

        return PageTransform.apply(
            repository.findAllByUserIdAndResult_Status(userId, status, pageable),
            ScanTransform::apply
        );
    }

    public Scan createScan(
        String userId,
        String url
    ) {
        return ScanTransform.apply(
            repository.save(
                org.timekeeper.database.postgresql.model.Scan.builder()
                    .userId(userId)
                    .result(
                        org.timekeeper.database.postgresql.model.ScanResult.builder()
                            .url(url)
                            .build()
                    )
                    .build()
            )
        );
    }

    public Scan createScan(
        String userId,
        String url,
        ScanResult result
    ) {
        return ScanTransform.apply(
            repository.save(
                org.timekeeper.database.postgresql.model.Scan.builder()
                    .userId(userId)
                    .result(
                        org.timekeeper.database.postgresql.model.ScanResult.builder()
                            .id(result.getId())
                            .build()
                    )
                    .build()
            )
        );
    }

    public void deleteScan(Long scanId) {
        repository.deleteById(scanId);
    }

}
