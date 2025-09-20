package org.timekeeper.database.postgresql.model.transform;

import org.springframework.data.domain.PageRequest;

public final class PageRequestTransform {

    public static PageRequest apply(org.timekeeper.model.request.PageRequest from) {
        return PageRequest.of(from.getPage(), from.getPageSize());
    }

}
