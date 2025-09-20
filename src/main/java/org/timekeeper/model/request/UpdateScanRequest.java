package org.timekeeper.model.request;

import lombok.Builder;
import lombok.Value;
import org.timekeeper.model.ScanStatus;

@Value
@Builder(toBuilder = true)
public class UpdateScanRequest {

    ScanStatus status;

}
