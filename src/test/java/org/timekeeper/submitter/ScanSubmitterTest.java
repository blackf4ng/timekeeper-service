package org.timekeeper.submitter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.timekeeper.client.UrlScanClient;
import org.timekeeper.client.model.SubmitScanResponse;
import org.timekeeper.model.Page;
import org.timekeeper.model.ScanResult;
import org.timekeeper.model.ScanResultStatus;
import org.timekeeper.model.request.PageRequest;
import org.timekeeper.service.ScanService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.timekeeper.submitter.ScanSubmitter.PAGE_SIZE;
import static org.timekeeper.submitter.ScanSubmitter.STATUS;

@ExtendWith(MockitoExtension.class)
public class ScanSubmitterTest {

    private static final Long SCAN_RESULT_ID_1 = 1L;

    private static final Long SCAN_RESULT_ID_2 = 3L;

    private static final Integer TOTAL_PAGES = 2;

    private static final Integer STATUS_CODE = 4;

    private static final String URL_1 = "url1";

    private static final String URL_2 = "url2";

    private static final String URL_SCAN_ID_1 = "urlScanId1";

    private static final String URL_SCAN_ID_2 = "urlScanId2";

    private static final String RESULT_URL_1 = "resultUrl1";

    private static final String RESULT_URL_2 = "resultUrl2";

    private static final String STATUS_MESSAGE = "statusMessage";

    private static final String STATUS_DESCRIPTION = "statusDescription";

    private static final ScanResult SCAN_RESULT_1 = ScanResult.builder()
        .id(SCAN_RESULT_ID_1)
        .url(URL_1)
        .build();

    private static final ScanResult SCAN_RESULT_2 = ScanResult.builder()
        .id(SCAN_RESULT_ID_2)
        .url(URL_2)
        .build();

    private static final PageRequest PAGE_REQUEST_1 = PageRequest.builder()
        .page(0)
        .pageSize(PAGE_SIZE)
        .build();

    @Mock
    private ScanService scanService;

    @Mock
    private UrlScanClient urlScanClient;

    @InjectMocks
    private ScanSubmitter scanSubmitter;

    @BeforeEach
    public void setupEach() {
        PageRequest pageRequest2 = PAGE_REQUEST_1.toBuilder()
            .page(1)
            .build();
        Page<ScanResult> page1 = Page.<ScanResult>builder()
            .data(List.of(SCAN_RESULT_1))
            .page(0)
            .totalPages(TOTAL_PAGES)
            .build();
        Page<ScanResult> page2 = page1.toBuilder()
            .data(List.of(SCAN_RESULT_2))
            .page(1)
            .build();

        lenient().when(scanService.listScanResults(STATUS, PAGE_REQUEST_1))
            .thenReturn(page1);
        lenient().when(scanService.listScanResults(STATUS, pageRequest2))
            .thenReturn(page2);
    }

    @Test
    public void testSubmit_withSuccessfulResponse_updatesStatusToProcessing() {
        ResponseEntity<SubmitScanResponse> responseEntity1 = new ResponseEntity<>(
            SubmitScanResponse.builder()
                .uuid(URL_SCAN_ID_1)
                .result(RESULT_URL_1)
                .build(),
            HttpStatus.OK
        );
        ResponseEntity<SubmitScanResponse> responseEntity2 = new ResponseEntity<>(
            SubmitScanResponse.builder()
                .uuid(URL_SCAN_ID_2)
                .result(RESULT_URL_2)
                .build(),
            HttpStatus.OK
        );
        when(urlScanClient.submitScan(URL_1)).thenReturn(responseEntity1);
        when(urlScanClient.submitScan(URL_2)).thenReturn(responseEntity2);

        scanSubmitter.submit();

        verify(scanService).updateScanResult(
            SCAN_RESULT_ID_1,
            ScanResultStatus.PROCESSING,
            Optional.empty(),
            Optional.of(URL_SCAN_ID_1),
            Optional.of(RESULT_URL_1)
        );
        verify(scanService).updateScanResult(
            SCAN_RESULT_ID_2,
            ScanResultStatus.PROCESSING,
            Optional.empty(),
            Optional.of(URL_SCAN_ID_2),
            Optional.of(RESULT_URL_2)
        );
    }

    @Test
    public void testSubmit_withNonRetryableClientFailures_updatesScanStatusToFailed() {
        Integer status2 = 10;
        String message2 = "message2";
        String description2 = "description2";
        ResponseEntity<SubmitScanResponse> responseEntity1 = new ResponseEntity<>(
            SubmitScanResponse.builder()
                .status(STATUS_CODE)
                .message(STATUS_MESSAGE)
                .description(STATUS_DESCRIPTION)
                .build(),
            HttpStatus.BAD_REQUEST
        );
        ResponseEntity<SubmitScanResponse> responseEntity2 = new ResponseEntity<>(
            SubmitScanResponse.builder()
                .status(status2)
                .message(message2)
                .description(description2)
                .build(),
            HttpStatus.BAD_REQUEST
        );

        when(urlScanClient.submitScan(URL_1)).thenReturn(responseEntity1);
        when(urlScanClient.submitScan(URL_2)).thenReturn(responseEntity2);

        scanSubmitter.submit();

        verify(scanService).updateScanResult(
            SCAN_RESULT_ID_1,
            ScanResultStatus.FAILED,
            Optional.of(
                ScanResult.StatusDetails.builder()
                    .code(STATUS_CODE)
                    .message(STATUS_MESSAGE)
                    .description(STATUS_DESCRIPTION)
                    .build()
            ),
            Optional.empty(),
            Optional.empty()
        );
        verify(scanService).updateScanResult(
            SCAN_RESULT_ID_2,
            ScanResultStatus.FAILED,
            Optional.of(
                ScanResult.StatusDetails.builder()
                    .code(status2)
                    .message(message2)
                    .description(description2)
                    .build()
            ),
            Optional.empty(),
            Optional.empty()
        );
    }

    @Test
    public void testSubmit_withClientThrottling_stopsProcessing() {
        ResponseEntity<SubmitScanResponse> responseEntity1 = new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        Optional<Instant> throttleReset = Optional.of(Instant.now());

        when(urlScanClient.submitScan(URL_1)).thenReturn(responseEntity1);
        when(urlScanClient.getThrottleReset(responseEntity1)).thenReturn(throttleReset);

        scanSubmitter.submit();

        verify(urlScanClient).submitScan(URL_1);
        verifyNoMoreInteractions(urlScanClient);
        verify(scanService).listScanResults(STATUS, PAGE_REQUEST_1);
        verifyNoMoreInteractions(scanService);
    }

    @Test
    public void testSubmit_withNonClientFailures_doesNotUpdateScanStatus() {
        ResponseEntity<SubmitScanResponse> responseEntity1 = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        ResponseEntity<SubmitScanResponse> responseEntity2 = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        when(urlScanClient.submitScan(URL_1)).thenReturn(responseEntity1);
        when(urlScanClient.submitScan(URL_2)).thenReturn(responseEntity2);

        scanSubmitter.submit();

        verify(scanService, never()).updateScanResult(any(), any(), any(), any(), any());
    }

}
