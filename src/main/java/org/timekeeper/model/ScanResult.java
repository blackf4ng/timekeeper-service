package org.timekeeper.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import net.minidev.json.annotate.JsonIgnore;

import java.time.Instant;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class ScanResult {

    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    public static class StatusDetails {
        Integer code;
        String message;
        String description;
    }

    @JsonIgnore
    Long id;

    String urlScanId;

    String url;

    String resultUrl;

    ScanResultStatus status;

    StatusDetails statusDetails;

    Instant createdAt;

    Instant updatedAt;

}
