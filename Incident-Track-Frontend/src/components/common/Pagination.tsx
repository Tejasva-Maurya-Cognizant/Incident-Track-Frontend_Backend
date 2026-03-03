interface PaginationProps {
    page: number;           // zero-based current page
    totalPages: number;
    totalElements: number;
    size: number;
    onPageChange: (newPage: number) => void;
    onSizeChange: (newSize: number) => void;
    pageSizeOptions?: number[];
}

export default function Pagination({
    page,
    totalPages,
    totalElements,
    size,
    onPageChange,
    onSizeChange,
    pageSizeOptions = [5, 10, 20, 50],
}: PaginationProps) {
    const from = totalElements === 0 ? 0 : page * size + 1;
    const to = Math.min((page + 1) * size, totalElements);

    const btnBase =
        "h-9 min-w-[36px] px-2 rounded-[8px] text-sm font-medium border transition-colors";
    const btnEnabled =
        "bg-white hover:bg-[#FAFCFF] text-slate-700 cursor-pointer";
    const btnDisabled =
        "bg-[#F8FAFD] text-slate-400 cursor-not-allowed";
    const btnActive =
        "bg-[#1E6FD9] text-white border-[#1E6FD9] cursor-default";

    // Build a compact page window: [1] ... [prev] [cur] [next] ... [last]
    const buildPages = (): (number | "…")[] => {
        if (totalPages <= 7) return Array.from({ length: totalPages }, (_, i) => i);
        const pages: (number | "…")[] = [];
        pages.push(0);
        if (page > 2) pages.push("…");
        for (let i = Math.max(1, page - 1); i <= Math.min(totalPages - 2, page + 1); i++) {
            pages.push(i);
        }
        if (page < totalPages - 3) pages.push("…");
        pages.push(totalPages - 1);
        return pages;
    };

    return (
        <div className="flex flex-wrap items-center justify-between gap-2 px-3 py-2 border-t bg-[#F8FAFD]" style={{ borderColor: "var(--border)" }}>
            {/* Left: rows info + page size */}
            <div className="flex items-center gap-2 text-xs text-slate-600">
                <span>
                    {totalElements === 0 ? "No results" : `${from}–${to} of ${totalElements}`}
                </span>
                <span className="hidden sm:inline text-slate-400">|</span>
                <label className="hidden sm:flex items-center gap-1.5">
                    Rows
                    <select
                        className="h-8 rounded-[8px] border px-2 text-sm bg-white text-slate-700 cursor-pointer"
                        style={{ borderColor: "var(--border)" }}
                        value={size}
                        onChange={(e) => {
                            onSizeChange(Number(e.target.value));
                        }}
                    >
                        {pageSizeOptions.map((o) => (
                            <option key={o} value={o}>{o}</option>
                        ))}
                    </select>
                </label>
            </div>

            {/* Right: page buttons */}
            {totalPages > 1 && (
                <div className="flex items-center gap-1">
                    {/* Prev */}
                    <button
                        className={`${btnBase} ${page === 0 ? btnDisabled : btnEnabled}`}
                        onClick={() => page > 0 && onPageChange(page - 1)}
                        disabled={page === 0}
                        aria-label="Previous page"
                    >
                        ‹
                    </button>

                    {buildPages().map((p, idx) =>
                        p === "…" ? (
                            <span key={`ellipsis-${idx}`} className="h-9 min-w-[36px] px-2 flex items-center justify-center text-slate-400 text-sm">
                                …
                            </span>
                        ) : (
                            <button
                                key={p}
                                className={`${btnBase} ${p === page ? btnActive : btnEnabled}`}
                                style={p !== page ? { borderColor: "var(--border)" } : undefined}
                                onClick={() => p !== page && onPageChange(p as number)}
                            >
                                {(p as number) + 1}
                            </button>
                        )
                    )}

                    {/* Next */}
                    <button
                        className={`${btnBase} ${page >= totalPages - 1 ? btnDisabled : btnEnabled}`}
                        onClick={() => page < totalPages - 1 && onPageChange(page + 1)}
                        disabled={page >= totalPages - 1}
                        aria-label="Next page"
                    >
                        ›
                    </button>
                </div>
            )}
        </div>
    );
}
