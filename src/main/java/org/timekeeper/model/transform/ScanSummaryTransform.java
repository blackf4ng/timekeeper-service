package org.timekeeper.model.transform;

import org.timekeeper.database.postgresql.model.ScanEntity;
import org.timekeeper.database.postgresql.model.ScanResultEntity;
import org.timekeeper.model.ScanSummary;

/**
 * Transforms into the internal Scan representation
 */
public final class ScanSummaryTransform {

    public static ScanSummary apply(ScanEntity from) {
        ScanResultEntity result = from.getResult();

        return ScanSummary.builder()
            .id(from.getId())
            .url(result.getUrl())
            .status(result.getStatus())
            .statusDetails(ScanResultStatusDetailsTransform.apply(result).orElse(null))
            .createdAt(from.getCreatedAt())
            .updatedAt(from.getUpdatedAt())
            .build();
    }

}
