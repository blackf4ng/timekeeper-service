package org.timekeeper.controller.internal;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.timekeeper.configuration.ClientConfiguration;
import org.timekeeper.controller.Controller;
import org.timekeeper.exception.ForbiddenException;
import org.timekeeper.model.Scan;
import org.timekeeper.model.request.UpdateScanRequest;
import org.timekeeper.service.ScanService;

@Slf4j
@RestController
@RequestMapping(value = "/internal/scans", produces = "application/json")
@RequiredArgsConstructor
public class InternalScanController implements Controller {

    private final ClientConfiguration clientConfiguration;

    private final ScanService scanService;

    @PatchMapping("/{scanId}")
    @Operation(summary = "Updates the details of a scan")
    public Scan updateScan(
        @AuthenticationPrincipal
        OidcUser user,
        @PathVariable Long scanId,
        @RequestBody UpdateScanRequest request
    ) {
        String userId = getUserId(user);
        log.info("Updating scan: userId={}, scanId={} request={}", userId, scanId, request);
        if(!clientConfiguration.getInternal().contains(userId)) {
            throw new ForbiddenException(
                String.format("User is not authorized to call endpoint: %s", userId)
            );
        }

        return scanService.updateScan(scanId);
    }

}
