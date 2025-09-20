package org.timekeeper.model;

import lombok.Builder;
import lombok.Value;
import net.minidev.json.annotate.JsonIgnore;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
public class ScanResult {

    @JsonIgnore
    Long id;

    String url;

    ScanResultStatus status;

    Instant createdAt;

    Instant updatedAt;

}
