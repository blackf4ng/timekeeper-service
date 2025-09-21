package org.timekeeper.client.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class SubmitScanRequest {
    String url;

    @Builder.Default
    ScanVisibility visibility = ScanVisibility.UNLISTED;

}
