import { useEffect, useState } from "react";
import { departmentsApi } from "../../features/departments/api";
import type { DepartmentResponseDto } from "../../features/departments/types";
import { getDepartmentId } from "../../features/departments/types";
import type { PageParams } from "../../types/pagination";
import Pagination from "../../components/common/Pagination";
import SortableHeader from "../../components/common/SortableHeader";

const DEFAULT_PARAMS: PageParams = { page: 0, size: 10, sortBy: "departmentName", sortDir: "asc" };

export default function AdminDepartmentsPage() {
  const [items, setItems] = useState<DepartmentResponseDto[]>([]);
  const [paging, setPaging] = useState({ totalElements: 0, totalPages: 0, page: 0 });
  const [params, setParams] = useState<PageParams>(DEFAULT_PARAMS);
  const [name, setName] = useState("");
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  const load = async (p: PageParams = params) => {
    setLoading(true);
    setErr(null);
    try {
      const res = await departmentsApi.listPaged(p);
      setItems(res.content);
      setPaging({ totalElements: res.totalElements, totalPages: res.totalPages, page: res.page });
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? "Failed to load departments");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(params); }, [params]);
  // eslint-disable-next-line react-hooks/exhaustive-deps

  const handleSort = (field: string) => {
    setParams((prev) => ({
      ...prev,
      page: 0,
      sortBy: field,
      sortDir: prev.sortBy === field && prev.sortDir === "asc" ? "desc" : "asc",
    }));
  };

  const onCreate = async () => {
    if (!name.trim()) return;
    try {
      await departmentsApi.create({ departmentName: name.trim() });
      setName("");
      await load(params);
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? "Create failed");
    }
  };

  return (
    <div className="flex flex-col h-full gap-3">
      <div>
        <h2 className="text-base font-semibold text-slate-900">Departments</h2>
        <p className="text-xs text-slate-500">Create and view departments.</p>
      </div>

      <div className="card p-3 sticky-bar">
        <div className="grid grid-cols-1 md:grid-cols-[1fr_auto] gap-2 md:items-end">
          <div>
            <label className="text-xs text-slate-700">Department name</label>
            <input
              className="input mt-0.5"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. IT"
              onKeyDown={(e) => e.key === "Enter" && onCreate()}
            />
          </div>
          <button className="btn-primary" type="button" onClick={onCreate}>
            Create
          </button>
        </div>
        {err && <div className="text-xs text-red-600 mt-2">{err}</div>}
      </div>

      <div className="card flex flex-col flex-1 overflow-hidden">
        <div className="overflow-x-auto flex-1">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-xs uppercase tracking-wide text-slate-500" style={{ position: "sticky", top: 0, zIndex: 5, background: "#F8FAFD" }}>
                <SortableHeader label="ID" field="departmentId" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} />
                <SortableHeader label="Name" field="departmentName" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} />
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td className="px-2 py-4 text-slate-600 text-xs" colSpan={2}>Loading...</td></tr>
              ) : items.length === 0 ? (
                <tr><td className="px-2 py-4 text-slate-600 text-xs" colSpan={2}>No departments found.</td></tr>
              ) : (
                items.map((d) => (
                  <tr key={getDepartmentId(d)} className="border-t hover:bg-[#FAFCFF]" style={{ borderColor: "var(--border)" }}>
                    <td className="px-2 py-2 font-medium text-slate-900 text-xs">{getDepartmentId(d)}</td>
                    <td className="px-2 py-2 text-slate-700 text-xs">{d.departmentName}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {paging.totalPages > 0 && (
          <Pagination
            page={paging.page}
            totalPages={paging.totalPages}
            totalElements={paging.totalElements}
            size={params.size}
            onPageChange={(newPage) => setParams((prev) => ({ ...prev, page: newPage }))}
            onSizeChange={(newSize) => setParams((prev) => ({ ...prev, size: newSize, page: 0 }))}
          />
        )}
      </div>

      <div className="text-xs text-slate-500">
        Note: Department update/delete UI needs backend endpoints (PUT/PATCH/DELETE).
      </div>
    </div>
  );
}