package org.timekeeper.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.timekeeper.model.Page;
import org.timekeeper.model.Scan;
import org.timekeeper.model.ScanResultStatus;
import org.timekeeper.model.ScanSummary;
import org.timekeeper.model.request.CreateScanRequest;
import org.timekeeper.model.request.PageRequest;
import org.timekeeper.exception.BadRequestException;
import org.timekeeper.service.ScanService;

import java.util.Optional;

import static org.timekeeper.database.postgresql.repository.Constants.MAX_PAGE_SIZE;

/**
 * Controller for servicing REST API calls to the /scans endpoint
 */
@Slf4j
@RestController
@RequestMapping(value = "scans", produces = "application/json")
@RequiredArgsConstructor
public class ScanController implements Controller {

    private final ScanService scanService;

    private final UrlValidator urlValidator;

    @GetMapping
    @Operation(summary = "Paginated API for listing all scans for the calling user")
    public Page<ScanSummary> listScanSummaries(
        @AuthenticationPrincipal
        OidcUser user,
        @RequestParam
        @Parameter(description = "Status of scans to filter on; not providing a status will return all statuses")
        Optional<ScanResultStatus> status,
        @RequestParam(defaultValue = "0")
        @Parameter(description = "0-indexed page offset for pagination")
        Integer page,
        @Parameter(description = "Number of items to query per page")
        @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        String userId = getUserId(user);
        log.info("Listing scan summaries: userId={} status={} page={} pageSize={}", userId, status, page, pageSize);
        if(page < 0) {
            throw new BadRequestException(
                String.format(
                    "Page number must be >= 0: page=%s", page)
            );
        }
        if(pageSize <= 0) {
            throw new BadRequestException(
                String.format(
                    "Page size must be > 0: pageSize=%s", pageSize)
            );
        }

        PageRequest pageRequest = PageRequest.builder()
            .page(page)
            .pageSize(Math.min(pageSize, MAX_PAGE_SIZE))
            .build();
        log.info("Listing scan summaries: userId={} status={} pageRequest={}", userId, status, pageRequest);

        return scanService.listScanSummaries(
            userId,
            status,
            pageRequest
        );
    }

    @GetMapping("/{scanId}")
    @Operation(summary = "Retrieves a scan by ID")
    public Scan getScan(
        @AuthenticationPrincipal OidcUser user,
        @PathVariable Long scanId
    ) {
        String userId = getUserId(user);
        log.info("Getting scan: userId={} scanId={}", userId, scanId);

        return scanService.getScan(userId, scanId);
    }

    @PostMapping
    @Operation(
        summary = "Creates a scan for the provided URL",
        description = "Input URL must be of a valid URL format"
    )
    public Scan createScan(
        @AuthenticationPrincipal OidcUser user,
        @RequestBody CreateScanRequest request
    ) {
        String userId = getUserId(user);
        log.info("Creating scan: userId={} request={}", userId, request);
        String url = request.getUrl();

        if (!urlValidator.isValid(url)) {
            throw new BadRequestException(
                String.format("URL is malformed: url=%s", url)
            );
        }
        return scanService.createScan(userId, request.getUrl());
    }

    @DeleteMapping("/{scanId}")
    @Operation(summary = "Deletes a scan by ID")
    public Void deleteScan(
        @AuthenticationPrincipal OidcUser user,
        @PathVariable Long scanId
    ) {
        String userId = getUserId(user);
        log.info("Deleting scan: userId={} scanId={}", userId, scanId);

        scanService.deleteScan(userId, scanId);
        return null;
    }

}
