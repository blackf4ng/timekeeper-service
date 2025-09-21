package org.timekeeper.client.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Getter
@RequiredArgsConstructor
public enum ScanVisibility {

    PUBLIC("public"),
    UNLISTED("unlisted"),
    PRIVATE("private");

    @JsonValue
    private final String value;
}
