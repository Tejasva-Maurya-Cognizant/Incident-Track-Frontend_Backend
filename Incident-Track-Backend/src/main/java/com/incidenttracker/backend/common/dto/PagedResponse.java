package com.incidenttracker.backend.common.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic paginated response wrapper used by all list endpoints.
 *
 * @param <T> the type of content items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    /** The list of items for the current page. */
    private List<T> content;

    /** Zero-based current page number. */
    private int page;

    /** Number of items per page. */
    private int size;

    /** Total number of items across all pages. */
    private long totalElements;

    /** Total number of pages. */
    private int totalPages;

    /** Whether this is the last page. */
    private boolean last;

    /** Whether this is the first page. */
    private boolean first;
}
