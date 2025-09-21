package org.timekeeper.model.transform;

import org.timekeeper.model.ScanResult;
import org.timekeeper.model.ScanResultStatus;

/**
 * Transforms into the internal ScanResult representation
 */
public final class ScanResultTransform {

    public static ScanResult apply(org.timekeeper.database.postgresql.model.ScanResult from) {
        return ScanResult.builder()
            .id(from.getId())
            .url(from.getUrl())
            .urlScanId(from.getUrlScanId())
            .resultUrl(from.getResultUrl())
            .status(from.getStatus())
            .statusDetails(ScanResultStatusDetailsTransform.apply(from).orElse(null))
            .createdAt(from.getCreatedAt())
            .updatedAt(from.getUpdatedAt())
            .build();
    }

}
