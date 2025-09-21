package org.timekeeper.database.postgresql.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.timekeeper.model.ScanResultStatus;

import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(
    name = "scan_result",
    indexes = {
        @Index(columnList = "url,createdAt"),
    })
public class ScanResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String url;

    @Column(length = 100)
    private String urlScanId;

    @Column(length = 100)
    private String resultUrl;

    @Builder.Default
    @Column(length = 25, nullable = false)
    private ScanResultStatus status = ScanResultStatus.SUBMITTED;

    @Column
    private Integer statusCode;

    @Column
    private String statusMessage;

    @Column
    private String statusDescription;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

}
