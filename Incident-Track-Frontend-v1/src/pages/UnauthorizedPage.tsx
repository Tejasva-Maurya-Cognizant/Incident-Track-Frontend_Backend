export default function UnauthorizedPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-[var(--bg)] px-4">
      <div className="card p-8 text-center max-w-md w-full">
        <h1 className="text-xl font-semibold text-slate-900">Access denied</h1>
        <p className="text-sm text-slate-600 mt-2">
          You don’t have permission to view this page.
        </p>
        <a href="/" className="btn-primary mt-6 inline-flex items-center justify-center">
          Back to Home
        </a>
      </div>
    </div>
  );
}