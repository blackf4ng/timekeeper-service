package org.timekeeper.database.postgresql.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.timekeeper.database.postgresql.model.Scan;
import org.timekeeper.model.ScanResultStatus;

import java.util.Optional;

public interface ScanRepository extends JpaRepository<Scan, Long>, PagingAndSortingRepository<Scan, Long> {

    Page<Scan> findAllByUserId(String userId, Pageable pageable);

    Page<Scan> findAllByUserIdAndResult_Status(String userId, ScanResultStatus status, Pageable pageable);

    Optional<Scan> findFirstByUserIdAndResult_Url(String userId, String url, Sort sort);

}
