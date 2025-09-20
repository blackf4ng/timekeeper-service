package org.timekeeper.model.transform;

import org.timekeeper.model.Scan;
import org.timekeeper.model.ScanResult;

/**
 * Transforms into the internal Scan representation
 */
public final class ScanResultTransform {

    public static ScanResult apply(org.timekeeper.database.postgresql.model.ScanResult from) {
        return ScanResult.builder()
            .id(from.getId())
            .url(from.getUrl())
            .status(from.getStatus())
            .createdAt(from.getCreatedAt())
            .updatedAt(from.getUpdatedAt())
            .build();
    }

}
