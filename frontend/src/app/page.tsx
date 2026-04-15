import Link from 'next/link';

export default function HomePage() {
  return (
    <section className="space-y-6 rounded-xl border bg-white p-8 shadow-sm">
      <h1 className="text-3xl font-semibold">Forms Builder</h1>
      <p className="text-slate-600">
        Build forms with multiple question types, publish them by link and inspect responses with aggregates.
      </p>
      <div className="flex gap-3">
        <Link href="/auth" className="rounded-lg bg-indigo-600 px-4 py-2 text-white hover:bg-indigo-500">
          Sign in
        </Link>
        <Link href="/dashboard" className="rounded-lg border px-4 py-2 hover:bg-slate-50">
          Dashboard
        </Link>
      </div>
    </section>
  );
}
