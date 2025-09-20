package org.timekeeper.database.postgresql.dal;

import org.springframework.data.domain.Sort;

public final class Constants {

    static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

}
