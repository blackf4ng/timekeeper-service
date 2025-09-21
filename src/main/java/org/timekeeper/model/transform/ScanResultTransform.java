package org.timekeeper.model.transform;

import org.timekeeper.database.postgresql.model.ScanResultEntity;
import org.timekeeper.model.ScanResult;

/**
 * Transforms into the internal ScanResult representation
 */
public final class ScanResultTransform {

    public static ScanResult apply(ScanResultEntity from) {
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
