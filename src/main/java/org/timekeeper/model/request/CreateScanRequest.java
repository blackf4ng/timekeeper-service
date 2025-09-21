package org.timekeeper.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Schema(description = "Request for creating a scan")
@Builder(toBuilder = true)
public class CreateScanRequest {

    @Schema(description = "URL to scan")
    String url;
}
