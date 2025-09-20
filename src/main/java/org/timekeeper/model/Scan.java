package org.timekeeper.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
public class Scan {

    Long id;

    @JsonIgnore
    String userId;

    String url;

    ScanStatus status;

    Instant createdAt;

    Instant updatedAt;

}
