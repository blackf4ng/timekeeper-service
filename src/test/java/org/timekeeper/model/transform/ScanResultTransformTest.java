package org.timekeeper.model.transform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.timekeeper.database.postgresql.model.ScanResultEntity;
import org.timekeeper.model.ScanResult;
import org.timekeeper.model.ScanResultStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ScanResultTransformTest {

    private static final Long ID = 1L;

    private static final String URL = "url";

    private static final ScanResultStatus STATUS = ScanResultStatus.FAILED;

    private static final Integer STATUS_CODE = 2;

    private static final String STATUS_MESSAGE = "statusMessage";

    private static final String STATUS_DESCRIPTION = "statusDescription";

    private static final String URL_SCAN_ID = "urlScanId";

    private static final String RESULT_URL = "resultUrl";

    private static final Instant CREATED_AT = Instant.now();

    private static final Instant UPDATED_AT = CREATED_AT.plus(1, ChronoUnit.DAYS);

    private static final ScanResultEntity SCAN_RESULT_ENTITY = ScanResultEntity.builder()
        .id(ID)
        .url(URL)
        .status(STATUS)
        .statusCode(STATUS_CODE)
        .statusMessage(STATUS_MESSAGE)
        .statusDescription(STATUS_DESCRIPTION)
        .urlScanId(URL_SCAN_ID)
        .resultUrl(RESULT_URL)
        .createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .build();

    private static final ScanResult SCAN_RESULT = ScanResult.builder()
        .id(ID)
        .url(URL)
        .urlScanId(URL_SCAN_ID)
        .resultUrl(RESULT_URL)
        .status(STATUS)
        .statusDetails(
            ScanResult.StatusDetails.builder()
                .code(STATUS_CODE)
                .description(STATUS_DESCRIPTION)
                .message(STATUS_MESSAGE)
                .build()
        ).createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .build();

    @Test
    public void testApply_withFailedStatus_includesStatusDetails() {
        assertEquals(
            SCAN_RESULT,
            ScanResultTransform.apply(SCAN_RESULT_ENTITY)
        );
    }

    @Test
    public void testApply_withNonFailedStatus_excludesStatusDetails() {
        ScanResultStatus status = ScanResultStatus.PROCESSING;

        assertEquals(
            SCAN_RESULT.toBuilder()
                .status(status)
                .statusDetails(null)
                .build(),
            ScanResultTransform.apply(
                SCAN_RESULT_ENTITY.toBuilder()
                    .status(status)
                    .build()
            )
        );
    }

}
