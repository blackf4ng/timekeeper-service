package org.timekeeper.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.timekeeper.database.postgresql.model.ScanEntity;
import org.timekeeper.database.postgresql.model.ScanResultEntity;
import org.timekeeper.database.postgresql.model.transform.PageRequestTransform;
import org.timekeeper.database.postgresql.repository.ScanRepository;
import org.timekeeper.database.postgresql.repository.ScanResultRepository;
import org.timekeeper.exception.DuplicateRequestException;
import org.timekeeper.exception.ResourceNotFoundException;
import org.timekeeper.model.Page;
import org.timekeeper.model.Scan;
import org.timekeeper.model.ScanResult;
import org.timekeeper.model.ScanResultStatus;
import org.timekeeper.model.ScanSummary;
import org.timekeeper.model.request.PageRequest;
import org.timekeeper.model.transform.ScanTransform;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.timekeeper.database.postgresql.repository.Constants.DEFAULT_REVERSE_SORT;
import static org.timekeeper.database.postgresql.repository.Constants.DEFAULT_SORT;
import static org.timekeeper.service.ScanService.SCAN_DEDUPE_DAYS;

@ExtendWith(MockitoExtension.class)
public class ScanServiceTest {

    private static final Long SCAN_RESULT_ID = 1L;

    private static final Long SCAN_ID = 2L;

    private static final Integer STATUS_CODE = 3;

    private static final Integer PAGE_SIZE = 4;

    private static final String USER_ID = "userId";

    private static final String URL = "url";

    private static final String URL_SCAN_ID = "urlScanId";

    private static final String RESULT_URL = "resultUrl1";

    private static final String STATUS_MESSAGE = "statusMessage";

    private static final String STATUS_DESCRIPTION = "statusDescription";

    private static final Instant NOW = Instant.now();

    private static final Instant DEDUPE_CUTOFF = NOW.minus(SCAN_DEDUPE_DAYS, ChronoUnit.DAYS);

    private static final Instant RESULT_CREATED_AT = NOW.plus(1, ChronoUnit.DAYS);

    private static final Instant RESULT_UPDATED_AT = NOW.plus(2, ChronoUnit.DAYS);

    private static final Instant SCAN_CREATED_AT = NOW.plus(3, ChronoUnit.DAYS);

    private static final Instant SCAN_UPDATED_AT = NOW.plus(4, ChronoUnit.DAYS);

    private static final ScanResultStatus STATUS = ScanResultStatus.SUBMITTED;

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
        .page(0)
        .pageSize(PAGE_SIZE)
        .build();

    @Mock
    org.springframework.data.domain.Page<ScanEntity> scanEntityPage;

    @Mock
    org.springframework.data.domain.Page<ScanResultEntity> scanResultEntityPage;

    @Mock
    Pageable pageable;

    @Mock
    private Clock clock;

    @Mock
    private ScanRepository scanRepository;

    @Mock
    private ScanResultRepository scanResultRepository;

    @InjectMocks
    private ScanService scanService;

    @Test
    public void testCreateScan_withoutPreviousScanNorScanResult_createsScan() {
        ScanEntity newScanEntity = ScanEntity.builder()
            .userId(USER_ID)
            .result(
                ScanResultEntity.builder()
                    .url(URL)
                    .build()
            ).build();

        when(clock.instant()).thenReturn(NOW);
        when(scanRepository.findFirstByUserIdAndResult_UrlAndCreatedAtAfter(USER_ID, URL, DEDUPE_CUTOFF, DEFAULT_SORT)).thenReturn(Optional.empty());
        when(scanResultRepository.findFirstByUrlAndCreatedAtAfter(URL, DEDUPE_CUTOFF, DEFAULT_SORT)).thenReturn(Optional.empty());
        when(scanRepository.save(newScanEntity)).thenReturn(newScanEntity);

        Scan actual = scanService.createScan(USER_ID, URL);

        assertEquals(ScanTransform.apply(newScanEntity), actual);
        verify(scanRepository).save(newScanEntity);
    }

    @Test
    public void testCreateScan_withPreviousScan_throwsDuplicateRequestException() {
        when(clock.instant()).thenReturn(NOW);
        when(scanRepository.findFirstByUserIdAndResult_UrlAndCreatedAtAfter(USER_ID, URL, DEDUPE_CUTOFF, DEFAULT_SORT)).thenReturn(Optional.of(SCAN_ENTITY));

        assertThrows(
            DuplicateRequestException.class,
            () -> scanService.createScan(USER_ID, URL)
        );
        verify(scanRepository, never()).save(any());
        verifyNoInteractions(scanResultRepository);
    }

    @Test
    public void testCreateScan_withPreviousScanResult_reusesScanResult() {
        ScanEntity newScanEntity = ScanEntity.builder()
            .userId(USER_ID)
            .result(SCAN_RESULT_ENTITY)
            .build();

        when(clock.instant()).thenReturn(NOW);
        when(scanRepository.findFirstByUserIdAndResult_UrlAndCreatedAtAfter(USER_ID, URL, DEDUPE_CUTOFF, DEFAULT_SORT)).thenReturn(Optional.empty());
        when(scanResultRepository.findFirstByUrlAndCreatedAtAfter(URL, DEDUPE_CUTOFF, DEFAULT_SORT)).thenReturn(Optional.of(SCAN_RESULT_ENTITY));
        when(scanRepository.save(newScanEntity)).thenReturn(newScanEntity);

        Scan actual = scanService.createScan(USER_ID, URL);
        assertEquals(ScanTransform.apply(newScanEntity), actual);
        verify(scanRepository).save(newScanEntity);
    }

    @Test
    public void testGetScan_withExistingScan_returnsScan() {
        when(scanRepository.findByIdAndUserId(SCAN_ID, USER_ID)).thenReturn(Optional.of(SCAN_ENTITY));

        assertEquals(SCAN, scanService.getScan(USER_ID, SCAN_ID));
    }

    @Test
    public void testGetScan_withoutExistingScan_throwsResourceNotFoundException() {
        when(scanRepository.findByIdAndUserId(SCAN_ID, USER_ID)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> scanService.getScan(USER_ID, SCAN_ID)
        );
    }

    @Test
    public void testListScanSummaries_withStatus_shouldSucceed() {
        org.springframework.data.domain.PageRequest pageRequest = PageRequestTransform.apply(PAGE_REQUEST)
            .withSort(DEFAULT_SORT);
        Integer pageNumber = 0;
        Integer totalPages = 1;
        Long totalElements = 2L;

        when(scanRepository.findAllByUserIdAndResult_Status(USER_ID, STATUS, pageRequest))
            .thenReturn(scanEntityPage);
        when(scanEntityPage.getTotalElements()).thenReturn(totalElements);
        when(scanEntityPage.getTotalPages()).thenReturn(totalPages);
        when(scanEntityPage.getPageable()).thenReturn(pageable);
        when(pageable.getPageNumber()).thenReturn(pageNumber);
        when(scanEntityPage.getContent()).thenReturn(List.of(SCAN_ENTITY));

        Page<ScanSummary> actual = scanService.listScanSummaries(USER_ID, Optional.of(STATUS), PAGE_REQUEST);

        assertEquals(
            Page.builder()
                .data(List.of(SCAN_SUMMARY))
                .page(pageNumber)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build(),
            actual
        );
    }

    @Test
    public void testListScanSummaries_withoutStatus_shouldSucceed() {
        org.springframework.data.domain.PageRequest pageRequest = PageRequestTransform.apply(PAGE_REQUEST)
            .withSort(DEFAULT_SORT);
        Integer pageNumber = 0;
        Integer totalPages = 1;
        Long totalElements = 2L;

        when(scanRepository.findAllByUserId(USER_ID, pageRequest))
            .thenReturn(scanEntityPage);
        when(scanEntityPage.getTotalElements()).thenReturn(totalElements);
        when(scanEntityPage.getTotalPages()).thenReturn(totalPages);
        when(scanEntityPage.getPageable()).thenReturn(pageable);
        when(pageable.getPageNumber()).thenReturn(pageNumber);
        when(scanEntityPage.getContent()).thenReturn(List.of(SCAN_ENTITY));

        Page<ScanSummary> actual = scanService.listScanSummaries(USER_ID, Optional.empty(), PAGE_REQUEST);

        assertEquals(
            Page.builder()
                .data(List.of(SCAN_SUMMARY))
                .page(pageNumber)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build(),
            actual
        );
    }

    @Test
    public void testDeleteScan_withExistingScan_deletesScan() {
        when(scanRepository.findByIdAndUserId(SCAN_ID, USER_ID)).thenReturn(Optional.of(SCAN_ENTITY));

        scanService.deleteScan(USER_ID, SCAN_ID);

        verify(scanRepository).deleteById(SCAN_ID);
    }

    @Test
    public void testDeleteScan_withoutExistingScan_doesNothing() {
        when(scanRepository.findByIdAndUserId(SCAN_ID, USER_ID)).thenReturn(Optional.empty());

        scanService.deleteScan(USER_ID, SCAN_ID);

        verify(scanRepository, never()).deleteById(any());
    }

    @Test
    public void testUpdateScanResult_withExistingScanResultWithOptionalFields_updatesScanResult() {
        ScanResultStatus newStatus = ScanResultStatus.FAILED;
        Integer newStatusCode = STATUS_CODE + 1;
        String newStatusMessage = "newMessage";
        String newStatusDescription = "newDescription";
        ScanResult.StatusDetails newStatusDetails = ScanResult.StatusDetails.builder()
            .code(newStatusCode)
            .message(newStatusMessage)
            .description(newStatusDescription)
            .build();
        String newUrlScanId = "newUrlScanId";
        String newResultUrl = "newResultUrl";
        ScanResultEntity newScanResultEntity = SCAN_RESULT_ENTITY.toBuilder()
            .status(newStatus)
            .statusCode(newStatusCode)
            .statusMessage(newStatusMessage)
            .statusDescription(newStatusDescription)
            .urlScanId(newUrlScanId)
            .resultUrl(newResultUrl)
            .build();

        when(scanResultRepository.findById(SCAN_RESULT_ID)).thenReturn(Optional.of(SCAN_RESULT_ENTITY));
        when(scanResultRepository.save(newScanResultEntity)).thenReturn(newScanResultEntity);

        ScanResult actual = scanService.updateScanResult(
            SCAN_RESULT_ID,
            newStatus,
            Optional.of(newStatusDetails),
            Optional.of(newUrlScanId),
            Optional.of(newResultUrl)
        );

        assertEquals(
            SCAN_RESULT.toBuilder()
                .status(newStatus)
                .statusDetails(newStatusDetails)
                .urlScanId(newUrlScanId)
                .resultUrl(newResultUrl)
                .build(),
            actual
        );
        verify(scanResultRepository).save(newScanResultEntity);
        verifyNoMoreInteractions(scanResultRepository);
    }

    @Test
    public void testUpdateScanResult_withExistingScanResultWithoutOptionalFields_updatesScanResult() {
        ScanResultStatus newStatus = ScanResultStatus.DONE;
        ScanResultEntity newScanResultEntity = SCAN_RESULT_ENTITY.toBuilder()
            .status(newStatus)
            .build();

        when(scanResultRepository.findById(SCAN_RESULT_ID)).thenReturn(Optional.of(SCAN_RESULT_ENTITY));
        when(scanResultRepository.save(newScanResultEntity)).thenReturn(newScanResultEntity);

        ScanResult actual = scanService.updateScanResult(
            SCAN_RESULT_ID,
            newStatus,
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );

        assertEquals(
            SCAN_RESULT.toBuilder()
                .status(newStatus)
                .build(),
            actual
        );
        verify(scanResultRepository).save(newScanResultEntity);
        verifyNoMoreInteractions(scanResultRepository);
    }

    @Test
    public void testUpdateScanResult_withoutExistingScanResult_throwsResourceNotFoundException() {
        when(scanResultRepository.findById(SCAN_RESULT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> scanService.updateScanResult(
                SCAN_RESULT_ID,
                ScanResultStatus.DONE,
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
            )
        );

        verify(scanResultRepository, never()).save(any());
    }

    @Test
    public void testListScanResults_withValidInput_shouldSucceed() {
        org.springframework.data.domain.PageRequest pageRequest = PageRequestTransform.apply(PAGE_REQUEST)
            .withSort(DEFAULT_REVERSE_SORT);
        Integer pageNumber = 0;
        Integer totalPages = 1;
        Long totalElements = 2L;

        when(scanResultRepository.findAllByStatus(STATUS, pageRequest))
            .thenReturn(scanResultEntityPage);
        when(scanResultEntityPage.getTotalElements()).thenReturn(totalElements);
        when(scanResultEntityPage.getTotalPages()).thenReturn(totalPages);
        when(scanResultEntityPage.getPageable()).thenReturn(pageable);
        when(pageable.getPageNumber()).thenReturn(pageNumber);
        when(scanResultEntityPage.getContent()).thenReturn(List.of(SCAN_RESULT_ENTITY));

        Page<ScanResult> actual = scanService.listScanResults(STATUS, PAGE_REQUEST);

        assertEquals(
            Page.builder()
                .data(List.of(SCAN_RESULT))
                .page(pageNumber)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build(),
            actual
        );
    }

}
