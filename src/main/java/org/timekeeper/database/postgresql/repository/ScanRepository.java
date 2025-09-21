package org.timekeeper.database.postgresql.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.timekeeper.database.postgresql.model.ScanEntity;
import org.timekeeper.model.ScanResultStatus;

import java.time.Instant;
import java.util.Optional;

public interface ScanRepository extends JpaRepository<ScanEntity, Long>, PagingAndSortingRepository<ScanEntity, Long> {

    Optional<ScanEntity> findByIdAndUserId(Long id, String userId);

    Page<ScanEntity> findAllByUserId(String userId, Pageable pageable);

    Page<ScanEntity> findAllByUserIdAndResult_Status(String userId, ScanResultStatus status, Pageable pageable);

    Optional<ScanEntity> findFirstByUserIdAndResult_UrlAndCreatedAtAfter(String userId, String url, Instant createdAt, Sort sort);

}
