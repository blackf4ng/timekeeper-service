package org.timekeeper.client.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmitScanResponse {

    String uuid;

    String country;

    ScanVisibility visibility;

    String url;

    String result;

    // Error response fields
    String message;

    String description;

    Integer status;
    
}
