package org.timekeeper.model.transform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.timekeeper.model.ScanResult;
import org.timekeeper.model.ScanResultStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ScanResultStatusDetailsTransformTest {

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

    private static final org.timekeeper.database.postgresql.model.ScanResult SCAN_RESULT_DATABASE = org.timekeeper.database.postgresql.model.ScanResult.builder()
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

    private static final ScanResult.StatusDetails STATUS_DETAILS = ScanResult.StatusDetails.builder()
        .code(STATUS_CODE)
        .description(STATUS_DESCRIPTION)
        .message(STATUS_MESSAGE)
        .build();

    @Test
    public void testApply_withValidInput_shouldSucceed() {
        assertEquals(
            Optional.of(STATUS_DETAILS),
            ScanResultStatusDetailsTransform.apply(SCAN_RESULT_DATABASE)
        );
    }

    @Test
    public void testApply_withNonFailedStatus_excludesStatusDetails() {
        ScanResultStatus status = ScanResultStatus.PROCESSING;

        assertEquals(
            Optional.empty(),
            ScanResultStatusDetailsTransform.apply(
                SCAN_RESULT_DATABASE.toBuilder()
                    .status(status)
                    .build()
            )
        );
    }

}
