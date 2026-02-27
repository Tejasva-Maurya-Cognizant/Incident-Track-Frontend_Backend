import { useState } from "react";
import { Outlet } from "react-router-dom";
import Sidebar from "./Sidebar";
import Header from "./Header";

export default function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="h-screen overflow-hidden flex flex-col" style={{ background: "var(--bg)" }}>
      <div className="flex flex-1 overflow-hidden w-full max-w-[var(--app-shell-max)] mx-auto px-2 py-2 gap-2 2xl:px-4 2xl:py-4 2xl:gap-4">

        {/* Desktop sidebar */}
        <aside className="hidden lg:flex flex-col w-[220px] 2xl:w-[240px] shrink-0">
          <Sidebar />
        </aside>

        {/* Mobile sidebar drawer overlay */}
        {sidebarOpen && (
          <div
            className="fixed inset-0 z-40 lg:hidden"
            onClick={() => setSidebarOpen(false)}
            style={{ background: "rgba(0,0,0,0.35)" }}
          />
        )}
        <aside
          className={`fixed inset-y-0 left-0 z-50 w-[220px] flex flex-col p-2 transition-transform duration-200 lg:hidden ${sidebarOpen ? "translate-x-0" : "-translate-x-full"
            }`}
          style={{ background: "var(--bg)" }}
        >
          <div className="flex justify-end mb-1">
            <button
              onClick={() => setSidebarOpen(false)}
              className="h-8 w-8 rounded-lg border flex items-center justify-center text-slate-600 hover:bg-[#FAFCFF]"
              style={{ borderColor: "var(--border)" }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <Sidebar />
        </aside>

        {/* Right column */}
        <div className="flex flex-col flex-1 overflow-hidden gap-2 min-w-0">
          <Header onMenuClick={() => setSidebarOpen(true)} />
          <main className="card flex-1 overflow-y-auto p-3 sm:p-4 2xl:p-5">
            <Outlet />
          </main>
        </div>

      </div>
    </div>
  );
}
