package org.timekeeper.poller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.timekeeper.client.UrlScanClient;
import org.timekeeper.client.model.GetResultResponse;
import org.timekeeper.model.Page;
import org.timekeeper.model.ScanResult;
import org.timekeeper.model.ScanResultStatus;
import org.timekeeper.model.request.PageRequest;
import org.timekeeper.service.ScanService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.timekeeper.poller.StatusPoller.PAGE_SIZE;
import static org.timekeeper.poller.StatusPoller.STATUS;

@ExtendWith(MockitoExtension.class)
public class StatusPollerTest {

    private static final Long SCAN_RESULT_ID_1 = 1L;

    private static final Long SCAN_RESULT_ID_2 = 3L;

    private static final Integer TOTAL_PAGES = 2;

    private static final Integer STATUS_CODE = 4;

    private static final String URL_SCAN_ID_1 = "urlScanId1";

    private static final String URL_SCAN_ID_2 = "urlScanId2";

    private static final String STATUS_MESSAGE = "statusMessage";

    private static final String STATUS_DESCRIPTION = "statusDescription";

    private static final ScanResult SCAN_RESULT_1 = ScanResult.builder()
        .id(SCAN_RESULT_ID_1)
        .urlScanId(URL_SCAN_ID_1)
        .build();

    private static final ScanResult SCAN_RESULT_2 = ScanResult.builder()
        .id(SCAN_RESULT_ID_2)
        .urlScanId(URL_SCAN_ID_2)
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
    private StatusPoller statusPoller;

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
    public void testPoll_withSuccessfulResponse_updatesStatusToDone() {
        ResponseEntity<GetResultResponse> responseEntity1 = new ResponseEntity<>(HttpStatus.OK);
        ResponseEntity<GetResultResponse> responseEntity2 = new ResponseEntity<>(HttpStatus.OK);
        when(urlScanClient.getResult(URL_SCAN_ID_1)).thenReturn(responseEntity1);
        when(urlScanClient.getResult(URL_SCAN_ID_2)).thenReturn(responseEntity2);

        statusPoller.poll();

        verify(scanService).updateScanResult(
            SCAN_RESULT_ID_1,
            ScanResultStatus.DONE,
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
            );
        verify(scanService).updateScanResult(
            SCAN_RESULT_ID_2,
            ScanResultStatus.DONE,
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );
    }

    @Test
    public void testPoll_withRequestInProgress_doesNotUpdateScanStatus() {
        ResponseEntity<GetResultResponse> responseEntity1 = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        ResponseEntity<GetResultResponse> responseEntity2 = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        when(urlScanClient.getResult(URL_SCAN_ID_1)).thenReturn(responseEntity1);
        when(urlScanClient.getResult(URL_SCAN_ID_2)).thenReturn(responseEntity2);
        when(urlScanClient.isInProgress(responseEntity1)).thenReturn(true);
        when(urlScanClient.isInProgress(responseEntity2)).thenReturn(true);

        statusPoller.poll();

        verify(scanService, never()).updateScanResult(any(), any(), any(), any(), any());
    }

    @Test
    public void testPoll_withNonRetryableClientFailures_updatesScanStatusToFailed() {
        Integer status2 = 10;
        String message2 = "message2";
        String description2 = "description2";
        ResponseEntity<GetResultResponse> responseEntity1 = new ResponseEntity<>(
            GetResultResponse.builder()
                .status(STATUS_CODE)
                .message(STATUS_MESSAGE)
                .description(STATUS_DESCRIPTION)
                .build(),
            HttpStatus.BAD_REQUEST
        );
        ResponseEntity<GetResultResponse> responseEntity2 = new ResponseEntity<>(
            GetResultResponse.builder()
                .status(status2)
                .message(message2)
                .description(description2)
                .build(),
            HttpStatus.BAD_REQUEST
        );

        when(urlScanClient.getResult(URL_SCAN_ID_1)).thenReturn(responseEntity1);
        when(urlScanClient.getResult(URL_SCAN_ID_2)).thenReturn(responseEntity2);
        when(urlScanClient.isInProgress(responseEntity1)).thenReturn(false);
        when(urlScanClient.isInProgress(responseEntity2)).thenReturn(false);

        statusPoller.poll();

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
    public void testPoll_withClientThrottling_stopsProcessing() {
        ResponseEntity<GetResultResponse> responseEntity1 = new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
        Optional<Instant> throttleReset = Optional.of(Instant.now());

        when(urlScanClient.getResult(URL_SCAN_ID_1)).thenReturn(responseEntity1);
        when(urlScanClient.getThrottleReset(responseEntity1)).thenReturn(throttleReset);

        statusPoller.poll();

        verify(urlScanClient).getResult(URL_SCAN_ID_1);
        verifyNoMoreInteractions(urlScanClient);
        verify(scanService).listScanResults(STATUS, PAGE_REQUEST_1);
        verifyNoMoreInteractions(scanService);
    }

    @Test
    public void testPoll_withNonClientFailures_doesNotUpdateScanStatus() {
        ResponseEntity<GetResultResponse> responseEntity1 = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        ResponseEntity<GetResultResponse> responseEntity2 = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        when(urlScanClient.getResult(URL_SCAN_ID_1)).thenReturn(responseEntity1);
        when(urlScanClient.getResult(URL_SCAN_ID_2)).thenReturn(responseEntity2);
        when(urlScanClient.isInProgress(responseEntity1)).thenReturn(false);
        when(urlScanClient.isInProgress(responseEntity2)).thenReturn(false);

        statusPoller.poll();

        verify(scanService, never()).updateScanResult(any(), any(), any(), any(), any());
    }

}
