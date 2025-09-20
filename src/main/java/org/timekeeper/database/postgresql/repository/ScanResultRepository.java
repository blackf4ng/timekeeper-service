package org.timekeeper.database.postgresql.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.timekeeper.database.postgresql.model.ScanResult;

import java.util.Optional;

public interface ScanResultRepository extends JpaRepository<ScanResult, Long>, PagingAndSortingRepository<ScanResult, Long> {

    Optional<ScanResult> findFirstByUrl(String url, Sort sort);

}
