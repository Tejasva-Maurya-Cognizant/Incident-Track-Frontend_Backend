package com.incidenttracker.backend.common.util;

import java.time.LocalDateTime;

public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static LocalDateTime nowTruncatedToSeconds() {
        return truncateToSeconds(LocalDateTime.now());
    }

    public static LocalDateTime truncateToSeconds(LocalDateTime value) {
        return value == null ? null : value.withNano(0);
    }
}
