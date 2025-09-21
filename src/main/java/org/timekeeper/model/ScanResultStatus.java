package org.timekeeper.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Enum of scan result statuses")
public enum ScanResultStatus {

    @Schema(description = "Represents that the scan was submitted by a user but not yet submitted to urlscan.io")
    SUBMITTED,
    @Schema(description = "Represents that the scan was submitted to urlscan.io and awaiting completion")
    PROCESSING,
    @Schema(description = "Represents that the scan was unable to be processed by urlscan.io")
    FAILED,
    @Schema(description = "Represents that the url was successfully scanned and results are ready")
    DONE,

}
