type ModalWindowProps = {
  title: string;
  subtitle?: string;
  onClose: () => void;
  children: React.ReactNode;
  maxWidthClassName?: string;
  bodyClassName?: string;
};

export default function ModalWindow({
  title,
  subtitle,
  onClose,
  children,
  maxWidthClassName = "max-w-xl",
  bodyClassName = "",
}: ModalWindowProps) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      style={{ background: "rgba(15,23,42,0.45)", backdropFilter: "blur(3px)" }}
      onClick={onClose}
    >
      <div
        className={`relative flex w-full flex-col overflow-hidden rounded-[14px] border bg-white shadow-2xl ${maxWidthClassName}`}
        style={{
          borderColor: "var(--border)",
          maxHeight: "calc(100vh - 2rem)",
          animation: "modalPopIn 0.2s cubic-bezier(0.16,1,0.3,1) both",
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div
          className="flex items-start justify-between gap-3 border-b px-5 py-4"
          style={{ borderColor: "var(--border)" }}
        >
          <div>
            <h3 className="text-sm font-semibold text-slate-900">{title}</h3>
            {subtitle && <p className="mt-0.5 text-xs text-slate-400">{subtitle}</p>}
          </div>
          <button
            onClick={onClose}
            className="flex h-8 w-8 shrink-0 items-center justify-center rounded-[8px] border text-slate-500 transition-colors hover:bg-[#FAFCFF]"
            style={{ borderColor: "var(--border)" }}
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div className={`flex-1 overflow-y-auto p-5 ${bodyClassName}`}>{children}</div>
      </div>

      <style>{`
        @keyframes modalPopIn {
          from { opacity: 0; transform: scale(0.93) translateY(12px); }
          to   { opacity: 1; transform: scale(1) translateY(0); }
        }
      `}</style>
    </div>
  );
}
