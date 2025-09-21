package org.timekeeper.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.timekeeper.client.model.GetResultResponse;
import org.timekeeper.client.model.SubmitScanRequest;
import org.timekeeper.client.model.SubmitScanResponse;
import org.timekeeper.configuration.client.UrlScanClientConfig;

import java.time.Instant;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
public class UrlScanClient {

    protected static final String THROTTLE_RESET_HEADER = "X-Rate-Limit-Reset";

    protected static final String IN_PROGRESS_MESSAGE = "Scan is not finished yet";

    private final RestClient client;

    public UrlScanClient(
        UrlScanClientConfig config,
        RestClient.ResponseSpec.ErrorHandler responseErrorHandler
    ) {
        this.client = RestClient.builder()
            .baseUrl(config.getUrl())
            .defaultHeader(config.getApiKeyHeader(), config.getApiKey())
            .defaultStatusHandler(statusCode -> true, responseErrorHandler)
            .build();
    }

    public ResponseEntity<SubmitScanResponse> submitScan(String url) {
        SubmitScanRequest request = SubmitScanRequest.builder()
            .url(url)
            .build();

        ResponseEntity<SubmitScanResponse> response = post("/scan", request, SubmitScanResponse.class);
        log.info("Submitted scan request: request={} response={}", request, response);

        return response;
    }

    public ResponseEntity<GetResultResponse> getResult(String scanId) {
        ResponseEntity<GetResultResponse> response = get(
            String.format("/result/%s", scanId),
            GetResultResponse.class
        );
        log.info("Retrieved scan result: scanId={} response={}", scanId, response);

        return response;
    }

    private <T> ResponseEntity<T> post(
        String uri,
        Object requestBody,
        Class<T> responseType
    ) {
        return client.post()
            .uri(uri)
            .contentType(APPLICATION_JSON)
            .body(requestBody)
            .retrieve()
            .toEntity(responseType);
    }

    private <T> ResponseEntity<T> get(
        String uri,
        Class<T> responseType
    ) {
        return client.get()
            .uri(uri)
            .retrieve()
            .toEntity(responseType);
    }

    public Optional<Instant> getThrottleReset(ResponseEntity<?> response) {
        return Optional.of(response.getHeaders())
            .map(headers -> headers.getFirst(THROTTLE_RESET_HEADER))
            .map(Instant::parse);
    }

    public boolean isInProgress(ResponseEntity<GetResultResponse> response) {
        return HttpStatus.NOT_FOUND.equals(response.getStatusCode()) &&
            IN_PROGRESS_MESSAGE.equals(response.getBody().getMessage());
    }

}
