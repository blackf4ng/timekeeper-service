package org.timekeeper.database.postgresql.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.timekeeper.database.postgresql.model.Scan;
import org.timekeeper.model.ScanStatus;

public interface ScanRepository extends JpaRepository<Scan, Long>, PagingAndSortingRepository<Scan, Long> {

    Page<Scan> findAllByUserId(String userId, Pageable pageable);

    Page<Scan> findAllByUserIdAndStatus(String userId, ScanStatus status, Pageable pageable);

}
