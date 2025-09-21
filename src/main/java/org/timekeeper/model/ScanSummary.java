package org.timekeeper.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
@Schema(description = "Summary of a scan request without result details")
public class ScanSummary {

    @Schema(description = "ID of the scan")
    Long id;

    @Schema(description = "URL ")
    String url;

    @Schema(description = "Status of the scan result")
    ScanResultStatus status;

    @Schema(description = "Details of status of the scan; present only if status is FAILED")
    ScanResult.StatusDetails statusDetails;

    @Schema(description = "Timestamp of when the scan was created")
    Instant createdAt;

    @Schema(description = "Timestamp of when the scan was last updated")
    Instant updatedAt;

}
