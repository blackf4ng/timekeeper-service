package org.timekeeper.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
public class ScanSummary {

    Long id;

    String url;

    ScanResultStatus status;

    ScanResult.StatusDetails statusDetails;

    Instant createdAt;

    Instant updatedAt;

}
