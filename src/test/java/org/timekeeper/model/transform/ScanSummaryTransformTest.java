package org.timekeeper.model.transform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.timekeeper.database.postgresql.model.Scan;
import org.timekeeper.model.ScanResult;
import org.timekeeper.model.ScanResultStatus;
import org.timekeeper.model.ScanSummary;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ScanSummaryTransformTest {

    private static final Long SCAN_RESULT_ID = 1L;

    private static final Long SCAN_ID = 2L;

    private static final Integer STATUS_CODE = 3;

    private static final String USER_ID = "userId";

    private static final String URL = "url";

    private static final ScanResultStatus STATUS = ScanResultStatus.FAILED;

    private static final String STATUS_MESSAGE = "statusMessage";

    private static final String STATUS_DESCRIPTION = "statusDescription";

    private static final String URL_SCAN_ID = "urlScanId";

    private static final String RESULT_URL = "resultUrl";

    private static final Instant RESULT_CREATED_AT = Instant.now();

    private static final Instant RESULT_UPDATED_AT = RESULT_CREATED_AT.plus(1, ChronoUnit.DAYS);

    private static final Instant SCAN_CREATED_AT = RESULT_CREATED_AT.plus(2, ChronoUnit.DAYS);

    private static final Instant SCAN_UPDATED_AT = RESULT_CREATED_AT.plus(3, ChronoUnit.DAYS);

    private static final org.timekeeper.database.postgresql.model.ScanResult SCAN_RESULT_DATABASE = org.timekeeper.database.postgresql.model.ScanResult.builder()
        .id(SCAN_RESULT_ID)
        .url(URL)
        .status(STATUS)
        .statusCode(STATUS_CODE)
        .statusMessage(STATUS_MESSAGE)
        .statusDescription(STATUS_DESCRIPTION)
        .urlScanId(URL_SCAN_ID)
        .resultUrl(RESULT_URL)
        .createdAt(RESULT_CREATED_AT)
        .updatedAt(RESULT_UPDATED_AT)
        .build();

    private static final Scan SCAN_DATABASE = Scan.builder()
        .id(SCAN_ID)
        .userId(USER_ID)
        .result(SCAN_RESULT_DATABASE)
        .createdAt(SCAN_CREATED_AT)
        .updatedAt(SCAN_UPDATED_AT)
        .build();

    private static final ScanSummary SCAN_SUMMARY = ScanSummary.builder()
        .id(SCAN_ID)
        .url(URL)
        .status(STATUS)
        .statusDetails(
            ScanResult.StatusDetails.builder()
                .code(STATUS_CODE)
                .description(STATUS_DESCRIPTION)
                .message(STATUS_MESSAGE)
                .build()
        ).createdAt(SCAN_CREATED_AT)
        .updatedAt(SCAN_UPDATED_AT)
        .build();

    @Test
    public void testApply_withFailedStatus_includesStatusDetails() {
        assertEquals(
            SCAN_SUMMARY,
            ScanSummaryTransform.apply(SCAN_DATABASE)
        );
    }

    @Test
    public void testApply_withNonFailedStatus_excludesStatusDetails() {
        ScanResultStatus status = ScanResultStatus.PROCESSING;

        assertEquals(
            SCAN_SUMMARY.toBuilder()
                .status(status)
                .statusDetails(null)
                .build(),
            ScanSummaryTransform.apply(
                SCAN_DATABASE.toBuilder()
                    .result(
                        SCAN_RESULT_DATABASE.toBuilder()
                            .status(status)
                            .build()
                    ).build()
            )
        );
    }

}
