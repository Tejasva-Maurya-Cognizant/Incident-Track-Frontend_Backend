import { type ReactNode, useEffect, useRef, useState } from "react";
import { useAuth } from "../../context/AuthContext";
import { authApi } from "../../features/auth/api";

type Tab = "info" | "edit" | "password";

interface Props {
    open: boolean;
    onClose: () => void;
}

const ROLE_COLOUR: Record<string, string> = {
    ADMIN: "bg-[#EEF2FF] text-[#4338CA]",
    MANAGER: "bg-[#FFF7ED] text-[#C2410C]",
    EMPLOYEE: "bg-[#ECFDF5] text-[#065F46]",
};

const STATUS_COLOUR: Record<string, string> = {
    ACTIVE: "bg-[#DCFDFC] text-[#0F766E]",
    INACTIVE: "bg-[#E6EDF5] text-slate-700",
    DEACTIVATED: "bg-[#FEE2E2] text-[#991B1B]",
};

function BigAvatar({ name }: { name: string }) {
    const initials = name
        .split(" ")
        .map((w) => w[0]?.toUpperCase() ?? "")
        .slice(0, 2)
        .join("");
    return (
        <div
            className="w-20 h-20 rounded-full flex items-center justify-center text-white text-3xl font-bold select-none ring-4 ring-white shadow-md"
            style={{ background: "var(--brand)" }}
        >
            {initials || "?"}
        </div>
    );
}

function InfoRow({ label, value, mono = false }: { label: string; value?: string | number | null; mono?: boolean }) {
    return (
        <div className="flex items-center justify-between py-2.5 border-b last:border-0" style={{ borderColor: "var(--border)" }}>
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wide w-32 shrink-0">{label}</span>
            <span className={`text-sm text-slate-800 font-medium text-right ${mono ? "font-mono" : ""}`}>{value ?? "—"}</span>
        </div>
    );
}

const NAV_ITEMS: { id: Tab; icon: ReactNode; label: string }[] = [
    {
        id: "info",
        label: "My Info",
        icon: (
            <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
        ),
    },
    {
        id: "edit",
        label: "Edit Profile",
        icon: (
            <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
        ),
    },
    {
        id: "password",
        label: "Change Password",
        icon: (
            <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
        ),
    },
];

export default function ProfilePanel({ open, onClose }: Props) {
    const { user, refreshProfile } = useAuth();
    const [tab, setTab] = useState<Tab>("info");

    /* edit form */
    const [editUsername, setEditUsername] = useState("");
    const [editSaving, setEditSaving] = useState(false);
    const [editErr, setEditErr] = useState<string | null>(null);
    const [editOk, setEditOk] = useState(false);

    /* password form */
    const [curPw, setCurPw] = useState("");
    const [newPw, setNewPw] = useState("");
    const [confirmPw, setConfirmPw] = useState("");
    const [pwSaving, setPwSaving] = useState(false);
    const [pwErr, setPwErr] = useState<string | null>(null);
    const [pwOk, setPwOk] = useState(false);
    const [showCur, setShowCur] = useState(false);
    const [showNew, setShowNew] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);

    const dialogRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (open && user) {
            setEditUsername(user.username ?? "");
            setEditErr(null);
            setEditOk(false);
            setPwErr(null);
            setPwOk(false);
            setCurPw("");
            setNewPw("");
            setConfirmPw("");
            setTab("info");
        }
    }, [open, user]);

    /* close on backdrop click */
    useEffect(() => {
        if (!open) return;
        const handler = (e: MouseEvent) => {
            if (dialogRef.current && !dialogRef.current.contains(e.target as Node)) onClose();
        };
        document.addEventListener("mousedown", handler);
        return () => document.removeEventListener("mousedown", handler);
    }, [open, onClose]);

    /* close on Escape */
    useEffect(() => {
        if (!open) return;
        const handler = (e: KeyboardEvent) => { if (e.key === "Escape") onClose(); };
        document.addEventListener("keydown", handler);
        return () => document.removeEventListener("keydown", handler);
    }, [open, onClose]);

    /* lock body scroll while open */
    useEffect(() => {
        document.body.style.overflow = open ? "hidden" : "";
        return () => { document.body.style.overflow = ""; };
    }, [open]);

    const onSaveEdit = async () => {
        if (!editUsername.trim()) return setEditErr("Username cannot be empty.");
        setEditSaving(true);
        setEditErr(null);
        setEditOk(false);
        try {
            await authApi.updateUser({ username: editUsername.trim() });
            await refreshProfile();
            setEditOk(true);
        } catch (e: any) {
            setEditErr(e?.response?.data?.message ?? "Update failed.");
        } finally {
            setEditSaving(false);
        }
    };

    const onChangePassword = async () => {
        setPwErr(null);
        setPwOk(false);
        if (!curPw) return setPwErr("Enter your current password.");
        if (newPw.length < 6) return setPwErr("New password must be at least 6 characters.");
        if (newPw !== confirmPw) return setPwErr("Passwords do not match.");
        setPwSaving(true);
        try {
            await authApi.updateUser({ username: user?.username ?? "", password: newPw });
            setPwOk(true);
            setCurPw(""); setNewPw(""); setConfirmPw("");
        } catch (e: any) {
            setPwErr(e?.response?.data?.message ?? "Password change failed.");
        } finally {
            setPwSaving(false);
        }
    };

    const EyeBtn = ({ show, onToggle }: { show: boolean; onToggle: () => void }) => (
        <button type="button" tabIndex={-1} onClick={onToggle}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600">
            <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                {show ? (
                    <>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </>
                ) : (
                    <>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                    </>
                )}
            </svg>
        </button>
    );

    const PwField = ({ label, value, onChange, show, onToggle, placeholder }: {
        label: string; value: string; onChange: (v: string) => void;
        show: boolean; onToggle: () => void; placeholder?: string;
    }) => (
        <div>
            <label className="text-sm font-medium text-slate-700">{label}</label>
            <div className="mt-1 relative">
                <input type={show ? "text" : "password"} className="input pr-10"
                    value={value} onChange={(e) => onChange(e.target.value)}
                    placeholder={placeholder} autoComplete="new-password" />
                <EyeBtn show={show} onToggle={onToggle} />
            </div>
        </div>
    );

    if (!open) return null;

    return (
        /* ── Full-screen overlay ── */
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4"
            style={{ background: "rgba(15,23,42,0.55)", backdropFilter: "blur(3px)" }}>

            {/* ── Modal window ── */}
            <div
                ref={dialogRef}
                className="relative w-full bg-white rounded-2xl shadow-2xl overflow-hidden flex flex-col"
                style={{
                    maxWidth: 780,
                    maxHeight: "90vh",
                    animation: "profilePopIn 0.2s cubic-bezier(0.16,1,0.3,1) both",
                }}
            >
                {/* ── TOP BANNER (gradient) ── */}
                <div className="relative h-28 shrink-0"
                    style={{ background: "linear-gradient(135deg, #175FFA 0%, #4F8EF7 60%, #7BB3F9 100%)" }}>
                    {/* close button */}
                    <button onClick={onClose}
                        className="absolute top-3 right-3 h-8 w-8 rounded-full bg-white/20 hover:bg-white/35 flex items-center justify-center text-white transition-colors">
                        <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                    {/* avatar anchored to bottom of banner */}
                    <div className="absolute -bottom-10 left-6">
                        <BigAvatar name={user?.username ?? "?"} />
                    </div>
                </div>

                {/* ── NAME / BADGES row (below banner) ── */}
                <div className="pt-12 pb-4 px-6 border-b flex items-end justify-between gap-3" style={{ borderColor: "var(--border)" }}>
                    <div>
                        <div className="text-xl font-bold text-slate-900 leading-tight">{user?.username ?? "—"}</div>
                        <div className="text-sm text-slate-500 mt-0.5">{user?.email ?? "—"}</div>
                    </div>
                    <div className="flex items-center gap-2 mb-0.5 flex-wrap justify-end">
                        <span className={`inline-flex px-2.5 py-1 rounded-full text-xs font-semibold ${ROLE_COLOUR[user?.role ?? ""] ?? "bg-slate-100 text-slate-600"}`}>
                            {user?.role ?? "—"}
                        </span>
                        <span className={`inline-flex px-2.5 py-1 rounded-full text-xs font-semibold ${STATUS_COLOUR[String(user?.status ?? "")] ?? "bg-slate-100 text-slate-600"}`}>
                            {String(user?.status ?? "—")}
                        </span>
                    </div>
                </div>

                {/* ── BODY: left nav + right content ── */}
                <div className="flex flex-1 overflow-hidden">

                    {/* Left nav */}
                    <nav className="w-44 shrink-0 border-r bg-[#F8FAFD] py-3 flex flex-col gap-0.5" style={{ borderColor: "var(--border)" }}>
                        {NAV_ITEMS.map((item) => (
                            <button
                                key={item.id}
                                onClick={() => setTab(item.id)}
                                className={`flex items-center gap-2.5 w-full text-left px-4 py-2.5 text-sm font-medium transition-colors rounded-lg mx-1 ${tab === item.id
                                    ? "bg-white text-[#175FFA] shadow-sm"
                                    : "text-slate-600 hover:bg-white/70 hover:text-slate-900"
                                    }`}
                                style={{ width: "calc(100% - 8px)" }}
                            >
                                <span className={tab === item.id ? "text-[#175FFA]" : "text-slate-400"}>{item.icon}</span>
                                {item.label}
                            </button>
                        ))}
                    </nav>

                    {/* Right content */}
                    <div className="flex-1 overflow-y-auto p-6">

                        {/* ── INFO ── */}
                        {tab === "info" && (
                            <div>
                                <h3 className="text-base font-semibold text-slate-900 mb-4">Account Information</h3>
                                <div className="px-3 rounded-xl border overflow-hidden" style={{ borderColor: "var(--border)" }}>
                                    <InfoRow label="User ID" value={user?.userId} mono />
                                    <InfoRow label="Username" value={user?.username} />
                                    <InfoRow label="Email" value={user?.email} />
                                    <InfoRow label="Role" value={user?.role} />
                                    <InfoRow label="Department ID" value={user?.departmentId} mono />
                                    <InfoRow label="Status" value={String(user?.status ?? "")} />
                                </div>
                                <p className="text-xs text-slate-400 mt-4">
                                    Email, department, and role are managed by your administrator.
                                </p>
                            </div>
                        )}

                        {/* ── EDIT ── */}
                        {tab === "edit" && (
                            <div className="space-y-5">
                                <h3 className="text-base font-semibold text-slate-900">Edit Profile</h3>

                                <div>
                                    <label className="text-sm font-medium text-slate-700 ">Username</label>
                                    <input
                                        className="input mt-1"
                                        value={editUsername}
                                        onChange={(e) => { setEditUsername(e.target.value); setEditOk(false); }}
                                        placeholder="Your display name"
                                    />
                                </div>

                                {/* Read-only grid */}
                                <div className="rounded-xl border overflow-hidden" style={{ borderColor: "var(--border)" }}>
                                    <div className="px-4 py-2.5 bg-[#F8FAFD] border-b text-xs font-semibold text-slate-500 uppercase tracking-wide" style={{ borderColor: "var(--border)" }}>
                                        Admin-managed fields (read only)
                                    </div>
                                    <div className="divide-y px-4" style={{ borderColor: "var(--border)" }}>
                                        <InfoRow label="Email" value={user?.email} />
                                        <InfoRow label="Role" value={user?.role} />
                                        <InfoRow label="Dept ID" value={user?.departmentId} mono />
                                        <InfoRow label="Status" value={String(user?.status ?? "")} />
                                    </div>
                                </div>

                                {editErr && (
                                    <div className="flex items-center gap-2 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
                                        <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                            <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01M21 12A9 9 0 113 12a9 9 0 0118 0z" />
                                        </svg>
                                        {editErr}
                                    </div>
                                )}
                                {editOk && (
                                    <div className="flex items-center gap-2 text-sm text-green-700 bg-green-50 border border-green-200 rounded-lg px-3 py-2">
                                        <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                            <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                                        </svg>
                                        Profile updated successfully.
                                    </div>
                                )}

                                <button className="btn-primary w-full" onClick={onSaveEdit} disabled={editSaving}>
                                    {editSaving ? "Saving…" : "Save Changes"}
                                </button>
                            </div>
                        )}

                        {/* ── PASSWORD ── */}
                        {tab === "password" && (
                            <div className="space-y-5">
                                <h3 className="text-base font-semibold text-slate-900">Change Password</h3>
                                <p className="text-sm text-slate-500">Choose a strong password of at least 6 characters.</p>

                                <PwField label="Current Password" value={curPw} onChange={setCurPw}
                                    show={showCur} onToggle={() => setShowCur(v => !v)} placeholder="Enter current password" />
                                <PwField label="New Password" value={newPw} onChange={setNewPw}
                                    show={showNew} onToggle={() => setShowNew(v => !v)} placeholder="At least 6 characters" />
                                <PwField label="Confirm New Password" value={confirmPw} onChange={setConfirmPw}
                                    show={showConfirm} onToggle={() => setShowConfirm(v => !v)} placeholder="Repeat new password" />

                                {/* Strength bar */}
                                {newPw.length > 0 && (
                                    <div className="space-y-1">
                                        <div className="flex gap-1">
                                            {[...Array(4)].map((_, i) => (
                                                <div key={i}
                                                    className={`h-1.5 flex-1 rounded-full transition-all ${i < Math.min(4, Math.floor(newPw.length / 3))
                                                        ? newPw.length < 6 ? "bg-red-400" : newPw.length < 9 ? "bg-yellow-400" : "bg-green-500"
                                                        : "bg-slate-200"
                                                        }`}
                                                />
                                            ))}
                                        </div>
                                        <div className="text-xs text-slate-400">
                                            Strength: <strong>{newPw.length < 6 ? "Weak" : newPw.length < 9 ? "Fair" : "Strong"}</strong>
                                        </div>
                                    </div>
                                )}

                                {pwErr && (
                                    <div className="flex items-center gap-2 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
                                        <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                            <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01M21 12A9 9 0 113 12a9 9 0 0118 0z" />
                                        </svg>
                                        {pwErr}
                                    </div>
                                )}
                                {pwOk && (
                                    <div className="flex items-center gap-2 text-sm text-green-700 bg-green-50 border border-green-200 rounded-lg px-3 py-2">
                                        <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                            <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                                        </svg>
                                        Password changed successfully.
                                    </div>
                                )}

                                <button className="btn-primary w-full" onClick={onChangePassword} disabled={pwSaving}>
                                    {pwSaving ? "Updating…" : "Change Password"}
                                </button>
                            </div>
                        )}

                    </div>
                </div>
            </div>

            {/* keyframe injected once */}
            <style>{`
        @keyframes profilePopIn {
          from { opacity: 0; transform: scale(0.93) translateY(12px); }
          to   { opacity: 1; transform: scale(1)    translateY(0); }
        }
      `}</style>
        </div>
    );
}
