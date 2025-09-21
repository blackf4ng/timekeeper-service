package org.timekeeper.model.transform;

import org.timekeeper.database.postgresql.model.ScanResult;
import org.timekeeper.model.ScanSummary;

/**
 * Transforms into the internal Scan representation
 */
public final class ScanSummaryTransform {

    public static ScanSummary apply(org.timekeeper.database.postgresql.model.Scan from) {
        ScanResult result = from.getResult();

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
