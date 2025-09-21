package org.timekeeper.database.postgresql.model.transform;

import org.timekeeper.database.postgresql.model.ScanResult;

import java.util.Optional;

public class ScanResultTransform {

    public static ScanResult apply(org.timekeeper.model.ScanResult from) {
        return ScanResult.builder()
            .id(from.getId())
            .url(from.getUrl())
            .status(from.getStatus())
            .urlScanId(from.getUrlScanId())
            .resultUrl(from.getResultUrl())
            .statusCode(
                Optional.ofNullable(from.getStatusDetails())
                    .map(org.timekeeper.model.ScanResult.StatusDetails::getCode)
                    .orElse(null)
            ).statusMessage(
                Optional.ofNullable(from.getStatusDetails())
                    .map(org.timekeeper.model.ScanResult.StatusDetails::getMessage)
                    .orElse(null)
            ).statusDescription(
                Optional.ofNullable(from.getStatusDetails())
                    .map(org.timekeeper.model.ScanResult.StatusDetails::getDescription)
                    .orElse(null)
            ).createdAt(from.getCreatedAt())
            .updatedAt(from.getUpdatedAt())
            .build();
    }

}
