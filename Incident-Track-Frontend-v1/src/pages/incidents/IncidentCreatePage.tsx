import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { incidentsApi } from "../../features/incidents/api";
import { categoriesApi } from "../../features/categories/api";
import type { CategoryResponseDto } from "../../features/categories/types";

export default function IncidentCreatePage() {
  const [categories, setCategories] = useState<CategoryResponseDto[]>([]);
  const [parentCategory, setParentCategory] = useState<string>("");
  const [categoryId, setCategoryId] = useState<number | null>(null);

  const [description, setDescription] = useState("");
  const [isCritical, setIsCritical] = useState(false);

  const [loadingCats, setLoadingCats] = useState(true);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    const loadCats = async () => {
      setLoadingCats(true);
      setErr(null);
      try {
        const list = await categoriesApi.list();
        setCategories(list.filter((c) => c.isVisible !== false));
      } catch (e: any) {
        setErr(e?.response?.data?.message ?? "Failed to load categories");
      } finally {
        setLoadingCats(false);
      }
    };
    loadCats();
  }, []);

  // Distinct parent category names
  const parentNames = useMemo(() => {
    const seen = new Set<string>();
    return categories
      .map((c) => c.categoryName)
      .filter((n) => { if (seen.has(n)) return false; seen.add(n); return true; });
  }, [categories]);

  // Sub-categories for the selected parent
  const subCategories = useMemo(() => {
    if (!parentCategory) return [];
    return categories.filter((c) => c.categoryName === parentCategory);
  }, [categories, parentCategory]);

  // When parent changes, reset child selection
  const handleParentChange = (name: string) => {
    setParentCategory(name);
    setCategoryId(null);
    setErr(null);
  };

  // When sub-category chosen, set categoryId
  const handleSubCategoryChange = (id: number) => {
    setCategoryId(id);
    setErr(null);
  };

  const selected = useMemo(
    () => categories.find((c) => c.categoryId === categoryId) ?? null,
    [categories, categoryId]
  );

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr(null);

    if (!parentCategory) return setErr("Please select a category.");
    if (!categoryId) return setErr("Please select a sub-category.");
    if (!description.trim()) return setErr("Description is required.");

    setLoading(true);
    try {
      const created = await incidentsApi.create({
        categoryId,
        description: description.trim(),
        isCritical,
      });
      window.location.href = `/incidents/${created.incidentId}`;
    } catch (error: any) {
      setErr(error?.response?.data?.message ?? "Failed to create incident");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl space-y-3">
      {/* Breadcrumb */}
      <div className="flex items-center gap-1.5 text-xs text-slate-500">
        <Link to="/incidents" className="hover:text-[#175FFA] transition-colors">Incidents</Link>
        <span>/</span>
        <span className="text-slate-900 font-medium">Create Incident</span>
      </div>

      <div>
        <h2 className="text-base font-semibold text-slate-900">Create Incident</h2>
        <p className="text-xs text-slate-500 mt-0.5">Report a new incident by selecting a category and sub-category.</p>
      </div>

      <form onSubmit={onSubmit} className="card p-4 space-y-4">

        {/* Category + Sub-category side-by-side, always reserve both columns */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {/* Parent category */}
          <div>
            <label className="text-xs font-medium text-slate-700">
              Category <span className="text-red-500">*</span>
            </label>
            {loadingCats ? (
              <div className="input mt-1 flex items-center text-slate-400 text-xs">Loading…</div>
            ) : (
              <select
                className="input mt-1 bg-white text-xs"
                value={parentCategory}
                onChange={(e) => handleParentChange(e.target.value)}
              >
                <option value="">— Select category —</option>
                {parentNames.map((name) => (
                  <option key={name} value={name}>{name}</option>
                ))}
              </select>
            )}
          </div>

          {/* Sub-category — always rendered, disabled when no parent */}
          <div>
            <label className="text-xs font-medium text-slate-700">
              Sub-Category <span className="text-red-500">*</span>
            </label>
            {!parentCategory ? (
              <select className="input mt-1 bg-white text-xs" disabled>
                <option>— Select category first —</option>
              </select>
            ) : subCategories.length === 0 ? (
              <select className="input mt-1 bg-white text-xs" disabled>
                <option>No sub-categories available</option>
              </select>
            ) : (
              <select
                className="input mt-1 bg-white text-xs"
                value={categoryId ?? ""}
                onChange={(e) => handleSubCategoryChange(Number(e.target.value))}
              >
                <option value="">— Select sub-category —</option>
                {subCategories.map((c) => (
                  <option key={c.categoryId} value={c.categoryId}>
                    {c.subCategory ?? c.categoryName}
                  </option>
                ))}
              </select>
            )}
          </div>
        </div>

        {/* Info card for selected category */}
        {selected && (
          <div
            className="rounded-[8px] border bg-[#F8FAFD] px-3 py-2 text-xs"
            style={{ borderColor: "var(--border)" }}
          >
            <span className="font-medium text-slate-800">{selected.categoryName}</span>
            {selected.subCategory && <span className="text-slate-500"> — {selected.subCategory}</span>}
            <span className="text-slate-400 ml-3">Dept: <span className="font-medium text-slate-600">{selected.departmentName ?? "—"}</span></span>
            <span className="text-slate-400 ml-3">SLA: <span className="font-medium text-slate-600">{selected.slaTimeHours ?? "—"}h</span></span>
          </div>
        )}

        {/* Description */}
        <div>
          <label className="text-xs font-medium text-slate-700">
            Description <span className="text-red-500">*</span>
          </label>
          <textarea
            className="input mt-1 h-20 resize-none py-2 text-xs"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Describe the issue clearly…"
          />
        </div>

        <label className="flex items-center gap-2 text-xs text-slate-700 cursor-pointer">
          <input type="checkbox" checked={isCritical} onChange={(e) => setIsCritical(e.target.checked)} />
          Mark as Critical (if urgency is high)
        </label>

        {err && (
          <div className="flex items-center gap-2 text-xs text-red-600 bg-red-50 border border-red-200 rounded-[8px] px-3 py-1.5">
            <svg xmlns="http://www.w3.org/2000/svg" className="w-3.5 h-3.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01M21 12A9 9 0 113 12a9 9 0 0118 0z" />
            </svg>
            {err}
          </div>
        )}

        <div className="flex items-center gap-2 pt-1">
          <button className="btn-primary h-8 text-xs px-4" disabled={loading || loadingCats}>
            {loading ? "Creating…" : "Create Incident"}
          </button>
          <Link
            to="/incidents"
            className="h-8 px-4 rounded-[8px] border text-xs font-medium text-slate-700 hover:bg-[#FAFCFF] transition-colors inline-flex items-center"
            style={{ borderColor: "var(--border)" }}
          >
            Cancel
          </Link>
        </div>
      </form>
    </div>
  );

}