package org.timekeeper.poller;

import lombok.RequiredArgsConstructor;
import org.timekeeper.service.ScanService;

@RequiredArgsConstructor
public class ScanStatusPoller {

    private final ScanService scanService;

    public void poll() {

    }

}
