package org.timekeeper.database.postgresql.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.timekeeper.database.postgresql.repository.ScanResultRepository;
import org.timekeeper.model.ScanResult;
import org.timekeeper.model.transform.ScanResultTransform;

import java.util.Optional;

import static org.timekeeper.database.postgresql.dal.Constants.DEFAULT_SORT;

@Component
@RequiredArgsConstructor
public class ScanResultDal {

    private final ScanResultRepository repository;

    public Optional<ScanResult> getLatestScanResultOptional(String url) {
        return repository.findFirstByUrl(url, DEFAULT_SORT)
            .map(ScanResultTransform::apply);
    }

}
