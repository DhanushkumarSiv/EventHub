import { Activity, Server } from "lucide-react";
import { useEffect, useState } from "react";
import { api } from "./services/api";

export default function App() {
  const [health, setHealth] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get("/health")
      .then((response) => {
        setHealth(response.data);
        setError("");
      })
      .catch((err) => {
        setHealth(null);
        setError(
          err.response?.data?.error ||
            "Cannot reach the EventHub API. Start the backend on port 8080."
        );
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <main className="min-h-screen bg-slate-950 text-slate-100">
      <div className="mx-auto flex max-w-3xl flex-col gap-8 px-6 py-16">
        <header>
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-indigo-400">
            Milestone 1
          </p>
          <h1 className="mt-2 text-4xl font-semibold tracking-tight">EventHub</h1>
          <p className="mt-3 text-slate-400">
            Campus Event Registration and Management System — local development
            skeleton.
          </p>
        </header>

        <section className="rounded-2xl border border-slate-800 bg-slate-900/70 p-6 shadow-xl">
          <div className="mb-4 flex items-center gap-2 text-slate-200">
            <Server className="h-5 w-5 text-indigo-400" />
            <h2 className="text-lg font-medium">Backend health</h2>
          </div>

          {loading && <p className="text-slate-400">Checking API…</p>}

          {!loading && error && (
            <p className="rounded-lg border border-red-900/60 bg-red-950/40 px-4 py-3 text-sm text-red-200">
              {error}
            </p>
          )}

          {!loading && health && (
            <dl className="grid grid-cols-1 gap-3 text-sm sm:grid-cols-2">
              <div className="rounded-lg bg-slate-800/80 p-3">
                <dt className="text-slate-400">Status</dt>
                <dd className="mt-1 flex items-center gap-2 font-medium text-emerald-400">
                  <Activity className="h-4 w-4" />
                  {health.status}
                </dd>
              </div>
              <div className="rounded-lg bg-slate-800/80 p-3">
                <dt className="text-slate-400">Database</dt>
                <dd className="mt-1 font-medium">{health.database}</dd>
              </div>
            </dl>
          )}
        </section>
      </div>
    </main>
  );
}
