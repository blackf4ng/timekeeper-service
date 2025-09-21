package org.timekeeper.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Jacksonized
@Schema(description = "A single page returned a part of a paginated list")
@Builder(toBuilder = true)
public class Page<T> {

    @Schema(description = "List of elements in the current page")
    List<T> data;

    @Schema(description = "Total number of elements in the full result")
    Long totalElements;

    @Schema(description = "0-indexed current page number")
    Integer page;

    @Schema(description = "Total number of pages in the full result")
    Integer totalPages;

}
