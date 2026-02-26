interface SortableHeaderProps {
    label: string;
    field: string;
    sortBy: string;
    sortDir: "asc" | "desc";
    onSort: (field: string) => void;
    className?: string;
}

export default function SortableHeader({
    label,
    field,
    sortBy,
    sortDir,
    onSort,
    className = "",
}: SortableHeaderProps) {
    const active = sortBy === field;

    return (
        <th
            className={`text-left px-2 py-2 cursor-pointer select-none whitespace-nowrap text-xs uppercase tracking-wide text-slate-500 ${className}`}
            onClick={() => onSort(field)}
        >
            <span className="inline-flex items-center gap-1 group">
                {label}
                <span
                    className={`text-[11px] leading-none transition-colors ${active ? "text-[#1E6FD9]" : "text-slate-400 group-hover:text-slate-600"
                        }`}
                >
                    {active ? (sortDir === "asc" ? "▲" : "▼") : "⇅"}
                </span>
            </span>
        </th>
    );
}
