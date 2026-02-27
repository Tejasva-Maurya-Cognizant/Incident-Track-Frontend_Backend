import { useCallback, useEffect, useState } from "react";
import { categoriesApi } from "../../features/categories/api";
import { departmentsApi } from "../../features/departments/api";
import type { CategoryResponseDto } from "../../features/categories/types";
import type { DepartmentResponseDto } from "../../features/departments/types";
import { getDepartmentId } from "../../features/departments/types";
import type { PageParams } from "../../types/pagination";
import Pagination from "../../components/common/Pagination";
import SortableHeader from "../../components/common/SortableHeader";


// ── Visibility badge ──────────────────────────────────────────────────────────
function VisibilityBadge({ visible }: { visible: boolean }) {
  return (
    <span
      className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold ${visible ? "bg-[#DCFDFC] text-[#0F766E]" : "bg-[#E6EDF5] text-slate-700"
        }`}
    >
      {visible ? "VISIBLE" : "HIDDEN"}
    </span>
  );
}

// ── Create modal ──────────────────────────────────────────────────────────────
interface CreateModalProps {
  departments: DepartmentResponseDto[];
  onClose: () => void;
  onCreated: () => void;
}

function CreateModal({ departments, onClose, onCreated }: CreateModalProps) {
  const [categoryName, setCategoryName] = useState("");
  const [subCategory, setSubCategory] = useState("");
  const [slaTimeHours, setSlaTimeHours] = useState<number>(24);
  const [isVisible, setIsVisible] = useState(true);
  const [departmentId, setDepartmentId] = useState<number>(
    departments.length > 0 ? getDepartmentId(departments[0]) : 0
  );
  const [saving, setSaving] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const onCreate = async () => {
    if (!categoryName.trim()) return setErr("Category name is required.");
    if (!departmentId) return setErr("Please select a department.");
    setSaving(true);
    setErr(null);
    try {
      await categoriesApi.create({
        categoryName: categoryName.trim(),
        subCategory: subCategory.trim() || undefined,
        slaTimeHours,
        isVisible,
        departmentId,
      });
      onCreated();
      onClose();
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? "Create failed");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center"
      style={{ background: "rgba(0,0,0,0.4)" }}
    >
      <div className="card w-full max-w-md p-5 space-y-4 m-4">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-sm font-semibold text-slate-900">Create Category</h3>
            <p className="text-xs text-slate-400 mt-0.5">Add a new incident category</p>
          </div>
          <button
            onClick={onClose}
            className="h-7 w-7 rounded-lg border flex items-center justify-center text-slate-500 hover:bg-[#FAFCFF]"
            style={{ borderColor: "var(--border)" }}
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        <div className="space-y-3">
          <div>
            <label className="text-xs text-slate-600 font-medium">Category Name</label>
            <input className="input mt-0.5" value={categoryName} onChange={(e) => setCategoryName(e.target.value)} placeholder="e.g. Network Issues" />
          </div>
          <div>
            <label className="text-xs text-slate-600 font-medium">Subcategory <span className="text-slate-400 font-normal">(optional)</span></label>
            <input className="input mt-0.5" value={subCategory} onChange={(e) => setSubCategory(e.target.value)} placeholder="e.g. VPN" />
          </div>
          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="text-xs text-slate-600 font-medium">SLA Hours</label>
              <input className="input mt-0.5" type="number" min={1} value={slaTimeHours} onChange={(e) => setSlaTimeHours(Number(e.target.value))} />
            </div>
            <div>
              <label className="text-xs text-slate-600 font-medium">Department</label>
              <select className="input mt-0.5 bg-white" value={departmentId} onChange={(e) => setDepartmentId(Number(e.target.value))}>
                {departments.map((d) => (
                  <option key={getDepartmentId(d)} value={getDepartmentId(d)}>{d.departmentName}</option>
                ))}
              </select>
            </div>
          </div>
          <div>
            <label className="flex items-center gap-2 text-xs text-slate-600 font-medium cursor-pointer">
              <input type="checkbox" checked={isVisible} onChange={(e) => setIsVisible(e.target.checked)} />
              Visible in Create Incident
            </label>
          </div>
        </div>
        {err && <div className="text-xs text-red-600">{err}</div>}
        <div className="flex gap-2 pt-1">
          <button className="btn-primary flex-1" onClick={onCreate} disabled={saving}>
            {saving ? "Creating…" : "Create Category"}
          </button>
          <button className="flex-1 h-9 rounded-[8px] bg-white border text-xs font-medium hover:bg-[#FAFCFF]" style={{ borderColor: "var(--border)" }} onClick={onClose}>
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Edit modal ────────────────────────────────────────────────────────────────
interface EditModalProps {
  category: CategoryResponseDto;
  departments: DepartmentResponseDto[];
  onClose: () => void;
  onSaved: () => void;
}

function EditModal({ category, departments, onClose, onSaved }: EditModalProps) {
  const [categoryName, setCategoryName] = useState(category.categoryName ?? "");
  const [subCategory, setSubCategory] = useState(category.subCategory ?? "");
  const [slaTimeHours, setSlaTimeHours] = useState<number>(category.slaTimeHours ?? 24);
  const [isVisible, setIsVisible] = useState(category.isVisible !== false);
  const [departmentId, setDepartmentId] = useState<number>(
    departments.length > 0 ? getDepartmentId(departments[0]) : 0
  );
  const [saving, setSaving] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const onSave = async () => {
    if (!categoryName.trim()) return setErr("Category name is required.");
    setSaving(true);
    setErr(null);
    try {
      await categoriesApi.update(category.categoryId, {
        categoryName: categoryName.trim(),
        subCategory: subCategory.trim() || undefined,
        slaTimeHours,
        isVisible,
        departmentId,
      });
      onSaved();
      onClose();
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? "Update failed");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center"
      style={{ background: "rgba(0,0,0,0.4)" }}
    >
      <div className="card w-full max-w-md p-5 space-y-4 m-4">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-sm font-semibold text-slate-900">Edit Category</h3>
            <p className="text-xs text-slate-400 mt-0.5">#{category.categoryId} · {category.categoryName}</p>
          </div>
          <button
            onClick={onClose}
            className="h-7 w-7 rounded-lg border flex items-center justify-center text-slate-500 hover:bg-[#FAFCFF]"
            style={{ borderColor: "var(--border)" }}
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        <div className="space-y-3">
          <div>
            <label className="text-xs text-slate-600 font-medium">Category Name</label>
            <input className="input mt-0.5" value={categoryName} onChange={(e) => setCategoryName(e.target.value)} />
          </div>
          <div>
            <label className="text-xs text-slate-600 font-medium">Subcategory <span className="text-slate-400 font-normal">(optional)</span></label>
            <input className="input mt-0.5" value={subCategory} onChange={(e) => setSubCategory(e.target.value)} />
          </div>
          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="text-xs text-slate-600 font-medium">SLA Hours</label>
              <input className="input mt-0.5" type="number" min={1} value={slaTimeHours} onChange={(e) => setSlaTimeHours(Number(e.target.value))} />
            </div>
            <div>
              <label className="text-xs text-slate-600 font-medium">Department</label>
              <select className="input mt-0.5 bg-white" value={departmentId} onChange={(e) => setDepartmentId(Number(e.target.value))}>
                {departments.map((d) => (
                  <option key={getDepartmentId(d)} value={getDepartmentId(d)}>{d.departmentName}</option>
                ))}
              </select>
            </div>
          </div>
          <div>
            <label className="flex items-center gap-2 text-xs text-slate-600 font-medium cursor-pointer">
              <input type="checkbox" checked={isVisible} onChange={(e) => setIsVisible(e.target.checked)} />
              Visible in Create Incident
            </label>
          </div>
        </div>
        {err && <div className="text-xs text-red-600">{err}</div>}
        <div className="flex gap-2 pt-1">
          <button className="btn-primary flex-1" onClick={onSave} disabled={saving}>
            {saving ? "Saving…" : "Save Changes"}
          </button>
          <button className="flex-1 h-9 rounded-[8px] bg-white border text-xs font-medium hover:bg-[#FAFCFF]" style={{ borderColor: "var(--border)" }} onClick={onClose}>
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Main page ─────────────────────────────────────────────────────────────────
const DEFAULT_PARAMS: PageParams = { page: 0, size: 10, sortBy: "categoryName", sortDir: "asc" };

export default function AdminCategoriesPage() {
  const [items, setItems] = useState<CategoryResponseDto[]>([]);
  const [depts, setDepts] = useState<DepartmentResponseDto[]>([]);
  const [paging, setPaging] = useState({ totalElements: 0, totalPages: 0, page: 0 });
  const [params, setParams] = useState<PageParams>(DEFAULT_PARAMS);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  // Filters
  const [search, setSearch] = useState("");
  const [deptFilter, setDeptFilter] = useState<string>("");
  const [visibilityFilter, setVisibilityFilter] = useState<"" | "VISIBLE" | "HIDDEN">("");

  // Modals
  const [showCreate, setShowCreate] = useState(false);
  const [editCategory, setEditCategory] = useState<CategoryResponseDto | null>(null);

  const load = useCallback(async (p: PageParams) => {
    setLoading(true);
    setErr(null);
    try {
      const [cats, deps] = await Promise.all([
        categoriesApi.listPaged(p),
        departmentsApi.list(),
      ]);
      setItems(cats.content);
      setPaging({ totalElements: cats.totalElements, totalPages: cats.totalPages, page: cats.page });
      setDepts(deps);
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? "Failed to load data");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(params);
  }, [params, load]);

  const handleSort = (field: string) => {
    setParams((prev) => ({
      ...prev,
      page: 0,
      sortBy: field,
      sortDir: prev.sortBy === field && prev.sortDir === "asc" ? "desc" : "asc",
    }));
  };

  const handleToggle = async (id: number) => {
    try {
      await categoriesApi.toggleVisibility(id);
      await load(params);
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? "Toggle failed");
    }
  };

  // Client-side filter on loaded page
  const filtered = items.filter((c) => {
    if (
      search &&
      !String(c.categoryId).includes(search) &&
      !c.categoryName.toLowerCase().includes(search.toLowerCase()) &&
      !(c.subCategory ?? "").toLowerCase().includes(search.toLowerCase()) &&
      !(c.departmentName ?? "").toLowerCase().includes(search.toLowerCase())
    )
      return false;
    if (deptFilter && c.departmentName !== deptFilter) return false;
    if (visibilityFilter === "VISIBLE" && c.isVisible === false) return false;
    if (visibilityFilter === "HIDDEN" && c.isVisible !== false) return false;
    return true;
  });

  const uniqueDepts = Array.from(new Set(items.map((c) => c.departmentName).filter(Boolean)));

  return (
    <>
      {showCreate && (
        <CreateModal
          departments={depts}
          onClose={() => setShowCreate(false)}
          onCreated={() => load(params)}
        />
      )}
      {editCategory && (
        <EditModal
          category={editCategory}
          departments={depts}
          onClose={() => setEditCategory(null)}
          onSaved={() => load(params)}
        />
      )}

      <div className="flex flex-col h-full gap-3">
        {/* Header */}
        <div className="flex items-center justify-between gap-2 shrink-0">
          <div>
            <h2 className="text-base font-semibold text-slate-900">Categories</h2>
            <p className="text-xs text-slate-400 mt-0.5">Create, edit, and toggle visibility of incident categories.</p>
          </div>
          <button
            onClick={() => setShowCreate(true)}
            className="btn-primary inline-flex items-center gap-1.5 shrink-0"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
            </svg>
            Add Category
          </button>
        </div>

        {/* Toolbar */}
        <div className="card p-2 flex items-center gap-2 flex-nowrap shrink-0">
          <div className="relative flex-[2] min-w-0">
            <svg
              className="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slate-400 pointer-events-none"
              xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}
            >
              <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-4.35-4.35M16.65 16.65A7.5 7.5 0 1 0 4.5 4.5a7.5 7.5 0 0 0 12.15 12.15z" />
            </svg>
            <input
              className="input pl-8 h-8 text-xs w-full"
              placeholder="Search by ID, name, subcategory, department…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <select
            className="input h-8 text-xs bg-white flex-1 min-w-0"
            value={deptFilter}
            onChange={(e) => setDeptFilter(e.target.value)}
          >
            <option value="">All Departments</option>
            {uniqueDepts.map((d) => (
              <option key={d} value={d!}>{d}</option>
            ))}
          </select>
          <select
            className="input h-8 text-xs bg-white flex-1 min-w-0"
            value={visibilityFilter}
            onChange={(e) => setVisibilityFilter(e.target.value as any)}
          >
            <option value="">All Visibility</option>
            <option value="VISIBLE">VISIBLE</option>
            <option value="HIDDEN">HIDDEN</option>
          </select>
          <span className="shrink-0 text-xs text-slate-400 whitespace-nowrap">
            {filtered.length} result{filtered.length !== 1 ? "s" : ""}
          </span>
          {(search || deptFilter || visibilityFilter) && (
            <button
              className="shrink-0 h-8 px-3 rounded-[8px] bg-white border text-xs text-slate-600 hover:bg-[#FAFCFF] whitespace-nowrap"
              style={{ borderColor: "var(--border)" }}
              onClick={() => { setSearch(""); setDeptFilter(""); setVisibilityFilter(""); }}
            >
              Clear filters
            </button>
          )}
        </div>

        {err && <div className="text-xs text-red-600 shrink-0">{err}</div>}

        {/* Table card */}
        <div className="card flex flex-col flex-1 overflow-hidden">
          <div className="overflow-x-auto flex-1">
            <table className="w-full text-sm min-w-[560px]">
              <thead>
                <tr
                  className="text-xs uppercase tracking-wide text-slate-500 border-b"
                  style={{ position: "sticky", top: 0, zIndex: 5, background: "#F8FAFD", borderColor: "var(--border)" }}
                >
                  <SortableHeader label="ID" field="categoryId" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="w-14" />
                  <SortableHeader label="Category" field="categoryName" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} />
                  <SortableHeader label="Subcategory" field="subCategory" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} />
                  <th className="text-left px-2 py-2 w-36">Department</th>
                  <SortableHeader label="SLA (hrs)" field="slaTimeHours" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="w-24" />
                  <th className="text-left px-2 py-2 w-24">Visibility</th>
                  <th className="text-right px-2 py-2 w-32">Actions</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td className="px-2 py-6 text-slate-400 text-xs" colSpan={7}>Loading…</td>
                  </tr>
                ) : filtered.length === 0 ? (
                  <tr>
                    <td className="px-2 py-6 text-slate-400 text-xs" colSpan={7}>
                      {search || deptFilter || visibilityFilter ? "No categories match your filters." : "No categories found."}
                    </td>
                  </tr>
                ) : (
                  filtered.map((c) => (
                    <tr key={c.categoryId} className="border-t hover:bg-[#FAFCFF] transition" style={{ borderColor: "var(--border)" }}>
                      <td className="px-2 py-2 text-xs font-mono text-slate-400">#{c.categoryId}</td>
                      <td className="px-2 py-2 text-xs font-semibold text-slate-900">{c.categoryName}</td>
                      <td className="px-2 py-2 text-xs text-slate-600">{c.subCategory ?? "—"}</td>
                      <td className="px-2 py-2 text-xs text-slate-600">{c.departmentName ?? "—"}</td>
                      <td className="px-2 py-2 text-xs text-slate-600">{c.slaTimeHours ?? "—"}h</td>
                      <td className="px-2 py-2">
                        <VisibilityBadge visible={c.isVisible !== false} />
                      </td>
                      <td className="px-2 py-2 text-right">
                        <div className="inline-flex gap-1">
                          <button
                            type="button"
                            onClick={() => setEditCategory(c)}
                            className="h-7 px-2.5 rounded-[6px] bg-white border text-xs font-medium hover:bg-[#FAFCFF] inline-flex items-center gap-1"
                            style={{ borderColor: "var(--border)" }}
                          >
                            <svg xmlns="http://www.w3.org/2000/svg" className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                              <path strokeLinecap="round" strokeLinejoin="round" d="M15.232 5.232l3.536 3.536M9 11l6-6 3 3-6 6H9v-3z" />
                            </svg>
                            Edit
                          </button>
                          <button
                            type="button"
                            onClick={() => handleToggle(c.categoryId)}
                            className="h-7 px-2.5 rounded-[6px] bg-white border text-xs font-medium hover:bg-[#FAFCFF]"
                            style={{ borderColor: "var(--border)" }}
                          >
                            Toggle
                          </button>
                        </div>
                      </td>
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
      </div>
    </>
  );
}