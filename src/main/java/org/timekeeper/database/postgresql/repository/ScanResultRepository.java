package org.timekeeper.database.postgresql.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.timekeeper.database.postgresql.model.ScanResultEntity;
import org.timekeeper.model.ScanResultStatus;

import java.time.Instant;
import java.util.Optional;

public interface ScanResultRepository extends JpaRepository<ScanResultEntity, Long>, PagingAndSortingRepository<ScanResultEntity, Long> {

    Optional<ScanResultEntity> findFirstByUrlAndCreatedAtAfter(String url, Instant createdAt, Sort sort);

    Page<ScanResultEntity> findAllByStatus(ScanResultStatus status, Pageable pageable);

}
