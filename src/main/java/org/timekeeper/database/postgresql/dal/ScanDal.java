package org.timekeeper.database.postgresql.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.timekeeper.database.postgresql.model.transform.PageRequestTransform;
import org.timekeeper.database.postgresql.repository.ScanRepository;
import org.timekeeper.model.Page;
import org.timekeeper.model.Scan;
import org.timekeeper.model.ScanStatus;
import org.timekeeper.model.request.PageRequest;
import org.timekeeper.model.transform.PageTransform;
import org.timekeeper.model.transform.ScanTransform;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ScanDal {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final ScanRepository scanRepository;

    public Optional<Scan> getScanOptional(Long id) {
        return scanRepository.findById(id)
            .map(ScanTransform::apply);
    }

    public org.timekeeper.model.Page<Scan> listScans(String userId, PageRequest pageRequest) {
        Pageable pageable = PageRequestTransform.apply(pageRequest)
            .withSort(DEFAULT_SORT);

        return PageTransform.apply(
            scanRepository.findAllByUserId(userId, pageable),
            ScanTransform::apply
        );
    }

    public Page<Scan> listScans(
        String userId,
        ScanStatus status,
        PageRequest pageRequest
    ) {
        Pageable pageable = PageRequestTransform.apply(pageRequest)
            .withSort(DEFAULT_SORT);

        return PageTransform.apply(
            scanRepository.findAllByUserIdAndStatus(userId, status, pageable),
            ScanTransform::apply
        );
    }

    public Scan createScan(
        String userId,
        String url
    ) {
        return ScanTransform.apply(
            scanRepository.save(
                org.timekeeper.database.postgresql.model.Scan.builder()
                    .userId(userId)
                    .url(url)
                    .build()
            )
        );
    }

    public void deleteScan(Long scanId) {
        scanRepository.deleteById(scanId);
    }

}
