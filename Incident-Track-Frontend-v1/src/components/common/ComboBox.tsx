import { useEffect, useMemo, useRef, useState } from "react";

type Option = {
  value: number;
  label: string;
  meta?: string; // optional extra info line
};

export default function ComboBox({
  label,
  placeholder,
  options,
  value,
  onChange,
  disabled,
}: {
  label: string;
  placeholder?: string;
  options: Option[];
  value: number | null;
  onChange: (v: number) => void;
  disabled?: boolean;
}) {
  const [open, setOpen] = useState(false);
  const [q, setQ] = useState("");
  const wrapRef = useRef<HTMLDivElement | null>(null);

  const selected = useMemo(
    () => options.find((o) => o.value === value) ?? null,
    [options, value]
  );

  const filtered = useMemo(() => {
    const s = q.trim().toLowerCase();
    if (!s) return options;
    return options.filter((o) => (o.label + " " + (o.meta ?? "")).toLowerCase().includes(s));
  }, [options, q]);

  useEffect(() => {
    const onDoc = (e: MouseEvent) => {
      if (!wrapRef.current) return;
      if (!wrapRef.current.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener("mousedown", onDoc);
    return () => document.removeEventListener("mousedown", onDoc);
  }, []);

  return (
    <div ref={wrapRef} className="relative">
      <label className="text-sm text-slate-700">{label}</label>

      {/* Selected / Input */}
      <button
        type="button"
        disabled={disabled}
        onClick={() => setOpen((s) => !s)}
        className="input mt-1 flex items-center justify-between bg-white"
      >
        <span className={selected ? "text-slate-900" : "text-slate-500"}>
          {selected ? selected.label : (placeholder ?? "Select...")}
        </span>
        <span className="text-slate-400">▾</span>
      </button>

      {/* Dropdown */}
      {open && !disabled && (
        <div
          className="absolute z-20 mt-2 w-full rounded-[12px] border bg-white shadow-card overflow-hidden"
          style={{ borderColor: "var(--border)" }}
        >
          <div className="p-2">
            <input
              className="input"
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Search..."
              autoFocus
            />
          </div>

          <div className="max-h-[260px] overflow-auto border-t" style={{ borderColor: "var(--border)" }}>
            {filtered.length === 0 ? (
              <div className="px-3 py-3 text-sm text-slate-600">No results</div>
            ) : (
              filtered.map((o) => {
                const active = o.value === value;
                return (
                  <button
                    type="button"
                    key={o.value}
                    onClick={() => {
                      onChange(o.value);
                      setOpen(false);
                      setQ("");
                    }}
                    className={`w-full text-left px-3 py-3 border-t hover:bg-[#FAFCFF] transition ${
                      active ? "bg-[#FAFCFF]" : ""
                    }`}
                    style={{ borderColor: "var(--border)" }}
                  >
                    <div className="text-sm font-medium text-slate-900">{o.label}</div>
                    {o.meta && <div className="text-xs text-slate-600 mt-1">{o.meta}</div>}
                  </button>
                );
              })
            )}
          </div>
        </div>
      )}
    </div>
  );
}
