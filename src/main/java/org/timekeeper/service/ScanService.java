package org.timekeeper.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.timekeeper.database.postgresql.dal.ScanDal;
import org.timekeeper.exception.ResourceNotFoundException;
import org.timekeeper.model.Page;
import org.timekeeper.model.Scan;
import org.timekeeper.model.ScanStatus;
import org.timekeeper.model.request.PageRequest;

import java.util.Optional;

/**
 * Service layer responsible for handling business logic related to scans
 */
@Service
@RequiredArgsConstructor
public class ScanService {

    private final ScanDal scanDal;

    /**
     * Creates a scan for the given user with the requested URL
     *
     * @param userId the ID of the user that is making the request
     * @param url    the URL that is requested to be scanned
     * @return the scan that was created
     */
    public Scan createScan(
        String userId,
        String url
    ) {
        return scanDal.createScan(
            userId, url
        );
    }

    /**
     * Retrieves the details of a scan by the given scan ID
     *
     * @param userId The ID of the user that's making the request
     * @param scanId The ID of the scan that is to be retrieved
     * @return the scan details for the scan with the provided ID
     * @throws ResourceNotFoundException if either the user does not have access to the scan or the scan does not exist. In the interest of security,
     *                                   a single ResourceNotFoundException is thrown instead of distinguishing between the scan existing and the user not being authorized to view it
     */
    public Scan getScan(
        String userId,
        Long scanId
    ) {
        return scanDal.getScanOptional(scanId)
            .filter(scan -> userId.equals(scan.getUserId()))
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    String.format("Scan not found: scanId=%s", scanId)
                )
            );
    }

    /**
     * Lists all scans for a given user sorted in descending order of when the scan was created at. Optionally filters the results by the status of the scan
     *
     * @param userId         The ID of the user that scans are to be retrieved for
     * @param statusOptional An optional status to filter the results for
     * @return A paginated list of scans meeting the filter criteria sorted in descending order of when the scan was created
     */
    public Page<Scan> listScans(
        String userId,
        Optional<ScanStatus> statusOptional,
        PageRequest pageRequest
    ) {
        return statusOptional
            .map(status -> scanDal.listScans(userId, status, pageRequest))
            .orElseGet(() -> scanDal.listScans(userId, pageRequest));
    }

    public Scan deleteScan(String userId, Long scanId) {
        Scan scan = getScan(userId, scanId);

        scanDal.deleteScan(scan.getId());

        return scan;
    }

    public Scan updateScan(Long scanId) {
        return Scan.builder()
            .id(scanId)
            .build();
    }

}
