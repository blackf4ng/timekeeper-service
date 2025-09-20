package org.timekeeper.model.request;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PageRequest {

    Integer page;

    Integer pageSize;

}
