package org.timekeeper.model.transform;

import org.timekeeper.model.ScanResult;
import org.timekeeper.model.ScanResultStatus;

import java.util.Optional;

/**
 * Transforms into the internal ScanResult.StatusDetails representation
 */
public final class ScanResultStatusDetailsTransform {

    public static Optional<ScanResult.StatusDetails> apply(org.timekeeper.database.postgresql.model.ScanResult from) {
        if (ScanResultStatus.FAILED.equals(from.getStatus())) {
            return Optional.of(
                ScanResult.StatusDetails.builder()
                .code(from.getStatusCode())
                .message(from.getStatusMessage())
                .description(from.getStatusDescription())
                .build()
            );
        }

        return Optional.empty();
    }

}
