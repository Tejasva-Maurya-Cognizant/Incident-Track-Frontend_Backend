import { useEffect, useMemo, useState } from "react";
import { categoriesApi } from "../../features/categories/api";
import { departmentsApi } from "../../features/departments/api";
import type { CategoryResponseDto } from "../../features/categories/types";
import type { DepartmentResponseDto } from "../../features/departments/types";
import { getDepartmentId } from "../../features/departments/types";
import type { PageParams } from "../../types/pagination";
import ComboBox from "../../components/common/ComboBox";
import Pagination from "../../components/common/Pagination";
import SortableHeader from "../../components/common/SortableHeader";

const DEFAULT_PARAMS: PageParams = { page: 0, size: 10, sortBy: "categoryName", sortDir: "asc" };

export default function AdminCategoriesPage() {
  const [items, setItems] = useState<CategoryResponseDto[]>([]);
  const [depts, setDepts] = useState<DepartmentResponseDto[]>([]);
  const [paging, setPaging] = useState({ totalElements: 0, totalPages: 0, page: 0 });
  const [params, setParams] = useState<PageParams>(DEFAULT_PARAMS);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  // form state (create/update)
  const [editingId, setEditingId] = useState<number | null>(null);
  const [categoryName, setCategoryName] = useState("");
  const [subCategory, setSubCategory] = useState("");
  const [slaTimeHours, setSlaTimeHours] = useState<number>(24);
  const [isVisible, setIsVisible] = useState(true);
  const [departmentId, setDepartmentId] = useState<number | null>(null);

  const deptOptions = useMemo(
    () =>
      depts.map((d) => ({
        value: getDepartmentId(d),
        label: `${d.departmentName} (ID: ${getDepartmentId(d)})`,
      })),
    [depts]
  );

  const load = async (p: PageParams = params) => {
    setLoading(true);
    setErr(null);
    try {
      const [cats, deps] = await Promise.all([categoriesApi.listPaged(p), departmentsApi.list()]);
      setItems(cats.content);
      setPaging({ totalElements: cats.totalElements, totalPages: cats.totalPages, page: cats.page });
      setDepts(deps);
      if (deps.length > 0 && departmentId == null) setDepartmentId(getDepartmentId(deps[0]));
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? "Failed to load data");
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

  const resetForm = () => {
    setEditingId(null);
    setCategoryName("");
    setSubCategory("");
    setSlaTimeHours(24);
    setIsVisible(true);
  };

  const startEdit = (c: CategoryResponseDto) => {
    setEditingId(c.categoryId);
    setCategoryName(c.categoryName ?? "");
    setSubCategory(c.subCategory ?? "");
    setSlaTimeHours(c.slaTimeHours ?? 24);
    setIsVisible(c.isVisible !== false);
    // departmentId not available in response dto, keep current selected dept
  };

  const onSave = async () => {
    if (!categoryName.trim()) return setErr("Category name required");
    if (!departmentId) return setErr("Select department");

    setErr(null);
    try {
      if (editingId == null) {
        await categoriesApi.create({
          categoryName: categoryName.trim(),
          subCategory: subCategory.trim() || undefined,
          slaTimeHours,
          isVisible,
          departmentId,
        });
      } else {
        await categoriesApi.update(editingId, {
          categoryName: categoryName.trim(),
          subCategory: subCategory.trim() || undefined,
          slaTimeHours,
          isVisible,
          departmentId,
        });
      }
      resetForm();
      await load(params);
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? "Save failed");
    }
  };

  const onToggle = async (id: number) => {
    try {
      await categoriesApi.toggleVisibility(id);
      await load(params);
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? "Toggle failed");
    }
  };

  return (
    <div className="space-y-3">
      <div>
        <h2 className="text-base font-semibold text-slate-900">Categories</h2>
        <p className="text-xs text-slate-500">Create, update, and toggle visibility.</p>
      </div>

      <div className="card p-3 space-y-3 sticky-bar">
        <div className="text-xs font-semibold text-slate-900">
          {editingId ? `Edit Category #${editingId}` : "Create Category"}
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
          <div>
            <label className="text-xs text-slate-700">Category name</label>
            <input className="input mt-0.5" value={categoryName} onChange={(e) => setCategoryName(e.target.value)} />
          </div>
          <div>
            <label className="text-xs text-slate-700">Subcategory</label>
            <input className="input mt-0.5" value={subCategory} onChange={(e) => setSubCategory(e.target.value)} />
          </div>

          <div>
            <label className="text-xs text-slate-700">SLA hours</label>
            <input className="input mt-0.5" type="number" min={1} value={slaTimeHours} onChange={(e) => setSlaTimeHours(Number(e.target.value))} />
          </div>

          <div className="flex items-end gap-2 pb-0.5">
            <label className="flex items-center gap-1.5 text-xs text-slate-700">
              <input type="checkbox" checked={isVisible} onChange={(e) => setIsVisible(e.target.checked)} />
              Visible in Create Incident
            </label>
          </div>

          <ComboBox
            label="Department"
            placeholder={loading ? "Loading..." : "Select department"}
            options={deptOptions}
            value={departmentId}
            onChange={(v) => setDepartmentId(v)}
            disabled={loading}
          />
        </div>

        {err && <div className="text-xs text-red-600">{err}</div>}

        <div className="flex gap-2">
          <button className="btn-primary" type="button" onClick={onSave}>
            {editingId ? "Update" : "Create"}
          </button>
          {editingId && (
            <button
              type="button"
              onClick={resetForm}
              className="h-9 px-3.5 rounded-[8px] bg-white border text-xs font-medium hover:bg-[#FAFCFF]"
              style={{ borderColor: "var(--border)" }}
            >
              Cancel
            </button>
          )}
        </div>
      </div>

      <div className="card overflow-x-auto">
        <table className="w-full text-sm min-w-[560px]">
          <thead>
            <tr className="text-xs uppercase tracking-wide text-slate-500" style={{ position: "sticky", top: 0, zIndex: 5, background: "#F8FAFD" }}>
              <SortableHeader label="ID" field="categoryId" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} />
              <SortableHeader label="Category" field="categoryName" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} />
              <SortableHeader label="Subcategory" field="subCategory" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} />
              <SortableHeader label="Dept" field="departmentName" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} />
              <SortableHeader label="SLA" field="slaTimeHours" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} />
              <th className="text-left px-2 py-2">Visible</th>
              <th className="text-right px-2 py-2">Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td className="px-2 py-4 text-slate-600 text-xs" colSpan={7}>Loading...</td></tr>
            ) : (
              items.map((c) => (
                <tr key={c.categoryId} className="border-t hover:bg-[#FAFCFF]" style={{ borderColor: "var(--border)" }}>
                  <td className="px-2 py-2 font-medium text-slate-900 text-xs">{c.categoryId}</td>
                  <td className="px-2 py-2 text-slate-700 text-xs">{c.categoryName}</td>
                  <td className="px-2 py-2 text-slate-700 text-xs">{c.subCategory ?? "—"}</td>
                  <td className="px-2 py-2 text-slate-700 text-xs">{c.departmentName ?? "—"}</td>
                  <td className="px-2 py-2 text-slate-700 text-xs">{c.slaTimeHours ?? "—"}h</td>
                  <td className="px-2 py-2">
                    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold ${c.isVisible !== false ? "bg-[#DCFDFC] text-[#0F766E]" : "bg-[#E6EDF5] text-slate-700"
                      }`}>
                      {c.isVisible !== false ? "VISIBLE" : "HIDDEN"}
                    </span>
                  </td>
                  <td className="px-2 py-2 text-right">
                    <div className="inline-flex gap-1">
                      <button
                        type="button"
                        onClick={() => startEdit(c)}
                        className="h-7 px-2 rounded-[6px] bg-white border text-xs font-medium hover:bg-[#FAFCFF]"
                        style={{ borderColor: "var(--border)" }}
                      >
                        Edit
                      </button>
                      <button
                        type="button"
                        onClick={() => onToggle(c.categoryId)}
                        className="h-7 px-2 rounded-[6px] bg-white border text-xs font-medium hover:bg-[#FAFCFF]"
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
  );
}