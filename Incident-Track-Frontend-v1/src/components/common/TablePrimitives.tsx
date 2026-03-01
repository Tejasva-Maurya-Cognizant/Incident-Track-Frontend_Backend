import type { ReactNode } from "react";

type TableHeaderCellProps = {
  children?: ReactNode;
  className?: string;
  align?: "left" | "right" | "center";
};

type TableBodyRowProps = {
  children: ReactNode;
  index: number;
  className?: string;
  onClick?: () => void;
};

type TableIdCellProps = {
  id: string | number | null | undefined;
  className?: string;
};

export const TABLE_HEADER_ROW_CLASS =
  "text-xs uppercase tracking-wide text-slate-500 border-b";

export const TABLE_HEADER_ROW_STYLE = {
  position: "sticky" as const,
  top: 0,
  zIndex: 5,
  background: "#F8FAFD",
  borderColor: "var(--border)",
};

export const TABLE_HEADER_CELL_CLASS =
  "px-2 py-2 text-xs font-medium uppercase tracking-wide text-slate-500";

export function TableHeaderCell({
  children,
  className = "",
  align = "left",
}: TableHeaderCellProps) {
  const alignClass =
    align === "right" ? "text-right" : align === "center" ? "text-center" : "text-left";

  return <th className={`${TABLE_HEADER_CELL_CLASS} ${alignClass} ${className}`}>{children}</th>;
}

export function TableBodyRow({
  children,
  index,
  className = "",
  onClick,
}: TableBodyRowProps) {
  return (
    <tr
      className={`border-t hover:bg-[#FAFCFF] transition-colors ${onClick ? "cursor-pointer" : ""} ${className}`}
      style={{
        borderColor: "var(--border)",
        background: index % 2 === 0 ? "white" : "#FAFCFF",
      }}
      onClick={onClick}
    >
      {children}
    </tr>
  );
}

export function TableIdCell({ id, className = "" }: TableIdCellProps) {
  return (
    <td className={`px-2 py-2 text-xs font-mono text-slate-400 ${className}`}>
      {id == null ? "—" : `#${id}`}
    </td>
  );
}
