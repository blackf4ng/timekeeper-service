package org.timekeeper.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Schema(description = "Request for specifying a page to be retrieved")
public class PageRequest {

    @Schema(description = "0-index page offset")
    Integer page;

    @Schema(description = "Size of each page")
    Integer pageSize;

}
