package org.timekeeper.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
@Schema(description = "Includes details on the scan request and the result of the scan")
public class Scan {

    @Schema(description = "ID of the scan")
    Long id;

    @JsonIgnore
    @Schema(description = "ID of the user that created the scan")
    String userId;

    @Schema(description = "Details of the scan result")
    ScanResult result;

    @Schema(description = "Timestamp of when the scan was created")
    Instant createdAt;

    @Schema(description = "Timestamp of when the scan was last updated")
    Instant updatedAt;

}
