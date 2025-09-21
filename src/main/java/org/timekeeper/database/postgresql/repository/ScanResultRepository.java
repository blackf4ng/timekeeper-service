package org.timekeeper.database.postgresql.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.timekeeper.database.postgresql.model.ScanResult;
import org.timekeeper.model.ScanResultStatus;

import java.util.Optional;

public interface ScanResultRepository extends JpaRepository<ScanResult, Long>, PagingAndSortingRepository<ScanResult, Long> {

    Optional<ScanResult> findFirstByUrl(String url, Sort sort);

    Page<ScanResult> findAllByStatus(ScanResultStatus status, Pageable pageable);

}
