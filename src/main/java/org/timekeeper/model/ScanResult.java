package org.timekeeper.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@Jacksonized
@Builder(toBuilder = true)
@Schema(description = "Includes details on the result of a scan")
public class ScanResult {

    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    @Schema(description = "Details on the status of the scan result")
    public static class StatusDetails {
        @Schema(description = "Code of the exception that was encountered")
        Integer code;
        @Schema(description = "Short message about the exception that was encountered")
        String message;
        @Schema(description = "Detailed description of the exception that was encountered")
        String description;
    }

    @Schema(description = "ID of the scan result")
    Long id;

    @Schema(description = "ID of the scan provided by urlscan.io")
    String urlScanId;

    @Schema(description = "URL that was scanned")
    String url;

    @Schema(description = "URL of a web view of the result")
    String resultUrl;

    @Schema(description = "Status of the scan result")
    ScanResultStatus status;

    @Schema(description = "Details of status of the scan; present only if status is FAILED")
    StatusDetails statusDetails;

    @Schema(description = "Timestamp of when the scan result was created")
    Instant createdAt;

    @Schema(description = "Timestamp of when the scan result was last updated")
    Instant updatedAt;

}
