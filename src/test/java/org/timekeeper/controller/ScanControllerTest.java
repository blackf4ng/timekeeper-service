package org.timekeeper.controller;

import org.apache.commons.validator.routines.UrlValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.timekeeper.exception.BadRequestException;
import org.timekeeper.model.Page;
import org.timekeeper.model.Scan;
import org.timekeeper.model.ScanResult;
import org.timekeeper.model.ScanResultStatus;
import org.timekeeper.model.ScanSummary;
import org.timekeeper.model.request.CreateScanRequest;
import org.timekeeper.model.request.PageRequest;
import org.timekeeper.service.ScanService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.timekeeper.database.postgresql.repository.Constants.MAX_PAGE_SIZE;

@ExtendWith(MockitoExtension.class)
public class ScanControllerTest {

    private static final Long SCAN_RESULT_ID = 1L;

    private static final Long SCAN_ID = 2L;

    private static final Integer STATUS_CODE = 3;

    private static final Integer PAGE_SIZE = 4;

    private static final Integer PAGE = 5;

    private static final Integer TOTAL_PAGES = 6;

    private static final Long TOTAL_ELEMENTS = 7L;

    private static final String USER_ID = "userId";

    private static final String URL = "url";

    private static final String URL_SCAN_ID = "urlScanId";

    private static final String RESULT_URL = "resultUrl1";

    private static final String STATUS_MESSAGE = "statusMessage";

    private static final String STATUS_DESCRIPTION = "statusDescription";

    private static final Instant NOW = Instant.now();

    private static final Instant RESULT_CREATED_AT = NOW.plus(1, ChronoUnit.DAYS);

    private static final Instant RESULT_UPDATED_AT = NOW.plus(2, ChronoUnit.DAYS);

    private static final Instant SCAN_CREATED_AT = NOW.plus(3, ChronoUnit.DAYS);

    private static final Instant SCAN_UPDATED_AT = NOW.plus(4, ChronoUnit.DAYS);

    private static final ScanResultStatus STATUS = ScanResultStatus.SUBMITTED;

    private static final ScanResult.StatusDetails SCAN_RESULT_STATUS_DETAILS = ScanResult.StatusDetails.builder()
        .code(STATUS_CODE)
        .message(STATUS_MESSAGE)
        .description(STATUS_DESCRIPTION)
        .build();

    private static final ScanResult SCAN_RESULT = ScanResult.builder()
        .id(SCAN_RESULT_ID)
        .url(URL)
        .urlScanId(URL_SCAN_ID)
        .resultUrl(RESULT_URL)
        .status(STATUS)
        .createdAt(RESULT_CREATED_AT)
        .updatedAt(RESULT_UPDATED_AT)
        .build();

    private static final Scan SCAN = Scan.builder()
        .id(SCAN_ID)
        .userId(USER_ID)
        .result(SCAN_RESULT)
        .createdAt(SCAN_CREATED_AT)
        .updatedAt(SCAN_UPDATED_AT)
        .build();

    private static final ScanSummary SCAN_SUMMARY = ScanSummary.builder()
        .id(SCAN_ID)
        .status(STATUS)
        .url(URL)
        .createdAt(SCAN_CREATED_AT)
        .updatedAt(SCAN_UPDATED_AT)
        .build();

    private static final PageRequest PAGE_REQUEST = PageRequest.builder()
        .page(PAGE)
        .pageSize(PAGE_SIZE)
        .build();

    private static final Page<ScanSummary> SCAN_SUMMARY_PAGE = Page.<ScanSummary>builder()
        .data(List.of(SCAN_SUMMARY))
        .page(PAGE)
        .totalElements(TOTAL_ELEMENTS)
        .totalPages(TOTAL_PAGES)
        .build();

    @Mock
    private OidcUser user;

    @Mock
    private ScanService scanService;

    @Mock
    UrlValidator urlValidator;

    @InjectMocks
    private ScanController scanController;

    @BeforeEach
    public void setupEach() {
        when(user.getSubject()).thenReturn(USER_ID);
    }

    @Test
    public void testListScanSummaries_withValidInput_shouldSucceed() {
        when(scanService.listScanSummaries(USER_ID, Optional.of(STATUS), PAGE_REQUEST))
            .thenReturn(SCAN_SUMMARY_PAGE);

        assertEquals(SCAN_SUMMARY_PAGE, scanController.listScanSummaries(user, Optional.of(STATUS), PAGE, PAGE_SIZE));
    }

    @Test
    public void testListScanSummaries_withInvalidPage_throwsBadRequestException() {
        assertThrows(
            BadRequestException.class,
            () -> scanController.listScanSummaries(user, Optional.of(STATUS), -1, PAGE_SIZE)
        );

        verifyNoInteractions(scanService);
    }

    @Test
    public void testListScanSummaries_withInvalidPageSize_throwsBadRequestException() {
        assertThrows(
            BadRequestException.class,
            () -> scanController.listScanSummaries(user, Optional.of(STATUS), PAGE, -1)
        );

        verifyNoInteractions(scanService);
    }

    @Test
    public void testListScanSummaries_withPageSizeGreaterThanMaxPageSize_reducesPageSize() {
        when(scanService.listScanSummaries(USER_ID, Optional.of(STATUS), PAGE_REQUEST.toBuilder().pageSize(MAX_PAGE_SIZE).build()))
            .thenReturn(SCAN_SUMMARY_PAGE);

        assertEquals(SCAN_SUMMARY_PAGE, scanController.listScanSummaries(user, Optional.of(STATUS), PAGE, MAX_PAGE_SIZE + 1));
    }

    @Test
    public void testGetScan_withValidInput_shouldSucceed() {
        when(scanService.getScan(USER_ID, SCAN_ID)).thenReturn(SCAN);

        assertEquals(SCAN, scanController.getScan(user, SCAN_ID));
    }

    @Test
    public void testCreateScan_withValidInput_shouldSucceed() {
        when(urlValidator.isValid(URL)).thenReturn(true);
        when(scanService.createScan(USER_ID, URL)).thenReturn(SCAN);

        assertEquals(SCAN, scanController.createScan(user, CreateScanRequest.builder().url(URL).build()));
        verify(scanService).createScan(USER_ID, URL);
    }

    @Test
    public void testCreateScan_withInvalidUrl_throwsBadRequestException() {
        when(urlValidator.isValid(URL)).thenReturn(false);

        assertThrows(
            BadRequestException.class,
            () -> scanController.createScan(user, CreateScanRequest.builder().url(URL).build())
        );

        verifyNoInteractions(scanService);
    }

    @Test
    public void testDeleteScan_withValidInput_shouldSucceed() {
        scanController.deleteScan(user, SCAN_ID);

        verify(scanService).deleteScan(USER_ID, SCAN_ID);
    }

}
