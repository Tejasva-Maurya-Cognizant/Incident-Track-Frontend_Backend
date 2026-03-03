/** Mirrors the backend PagedResponse<T> generic wrapper. */
export interface PagedResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
    first: boolean;
}

export interface PageParams {
    page: number;
    size: number;
    sortBy: string;
    sortDir: "asc" | "desc";
}
