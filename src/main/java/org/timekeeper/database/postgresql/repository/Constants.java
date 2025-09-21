package org.timekeeper.database.postgresql.repository;

import org.springframework.data.domain.Sort;

public final class Constants {

    public static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    public static final Sort DEFAULT_REVERSE_SORT = Sort.by(Sort.Direction.ASC, "createdAt");

    public static final Integer MAX_PAGE_SIZE = 20;

}
