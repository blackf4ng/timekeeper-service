package org.timekeeper.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class Page<T> {

    List<T> data;

    Long totalElements;

    Integer page;

    Integer totalPages;

}
