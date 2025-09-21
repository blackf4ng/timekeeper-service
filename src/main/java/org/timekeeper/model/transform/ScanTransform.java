package org.timekeeper.model.transform;

import org.timekeeper.database.postgresql.model.ScanEntity;
import org.timekeeper.model.Scan;

/**
 * Transforms into the internal Scan representation
 */
public final class ScanTransform {

    public static Scan apply(ScanEntity from) {
        return Scan.builder()
            .id(from.getId())
            .userId(from.getUserId())
            .result(ScanResultTransform.apply(from.getResult()))
            .createdAt(from.getCreatedAt())
            .updatedAt(from.getUpdatedAt())
            .build();
    }

}
