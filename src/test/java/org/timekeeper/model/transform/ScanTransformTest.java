package org.timekeeper.model.transform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.timekeeper.database.postgresql.model.ScanEntity;
import org.timekeeper.database.postgresql.model.ScanResultEntity;
import org.timekeeper.model.ScanResultStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ScanTransformTest {

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

    private static final ScanResultEntity SCAN_RESULT_ENTITY = ScanResultEntity.builder()
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

    private static final ScanEntity SCAN_ENTITY = ScanEntity.builder()
        .id(SCAN_ID)
        .userId(USER_ID)
        .result(SCAN_RESULT_ENTITY)
        .createdAt(SCAN_CREATED_AT)
        .updatedAt(SCAN_UPDATED_AT)
        .build();

    private static final org.timekeeper.model.Scan SCAN = org.timekeeper.model.Scan.builder()
        .id(SCAN_ID)
        .userId(USER_ID)
        .result(ScanResultTransform.apply(SCAN_RESULT_ENTITY))
        .createdAt(SCAN_CREATED_AT)
        .updatedAt(SCAN_UPDATED_AT)
        .build();

    @Test
    public void testApply_withValidInput_shouldSucceed() {
        assertEquals(
            SCAN,
            ScanTransform.apply(SCAN_ENTITY)
        );
    }

}
