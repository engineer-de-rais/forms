'use client';

import Link from 'next/link';
import { FormEvent, useEffect, useState } from 'react';
import { createForm, FormResponse, getForms, publishForm, unpublishForm } from '@/lib/api';
import { getStoredUserId } from '@/lib/session';

export default function DashboardPage() {
  const [userId, setUserId] = useState<number | null>(null);
  const [forms, setForms] = useState<FormResponse[]>([]);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const resolvedUserId = getStoredUserId();
    if (!resolvedUserId) {
      setError('Open /auth first and sign in.');
      return;
    }
    setUserId(resolvedUserId);
    void loadForms(resolvedUserId);
  }, []);

  async function loadForms(currentUserId: number) {
    try {
      const loaded = await getForms(currentUserId);
      setForms(loaded);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load forms');
    }
  }

  async function onCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!userId) {
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const form = await createForm(userId, title, description);
      setForms((prev) => [form, ...prev]);
      setTitle('');
      setDescription('');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to create form');
    } finally {
      setLoading(false);
    }
  }

  async function onTogglePublish(form: FormResponse) {
    if (!userId) {
      return;
    }
    try {
      const updated = form.status === 'PUBLISHED' ? await unpublishForm(userId, form.id) : await publishForm(userId, form.id);
      setForms((prev) => prev.map((candidate) => (candidate.id === form.id ? updated : candidate)));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to change publish status');
    }
  }

  return (
    <section className="space-y-6">
      <div className="rounded-xl border bg-white p-6 shadow-sm">
        <h1 className="text-2xl font-semibold">Forms Dashboard</h1>
        <p className="text-slate-600">Create forms, edit structure, publish and track responses.</p>
      </div>

      <form onSubmit={onCreate} className="space-y-3 rounded-xl border bg-white p-6 shadow-sm">
        <h2 className="text-lg font-semibold">Create a new form</h2>
        <input
          required
          className="w-full rounded-lg border px-3 py-2"
          placeholder="Form title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
        <textarea
          className="w-full rounded-lg border px-3 py-2"
          placeholder="Form description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
        <button className="rounded-lg bg-indigo-600 px-4 py-2 text-white disabled:opacity-60" disabled={loading}>
          {loading ? 'Creating...' : 'Create form'}
        </button>
      </form>

      {error && <p className="rounded-lg bg-red-50 p-3 text-red-700">{error}</p>}

      <div className="space-y-3">
        {forms.length === 0 ? (
          <p className="rounded-xl border bg-white p-6 text-slate-600 shadow-sm">No forms yet.</p>
        ) : (
          forms.map((form) => {
            const publicUrl = `/f/${form.slug}`;
            return (
              <article key={form.id} className="space-y-3 rounded-xl border bg-white p-6 shadow-sm">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <h3 className="text-lg font-semibold">{form.title}</h3>
                    <p className="text-sm text-slate-600">{form.description || 'No description'}</p>
                    <p className="text-xs text-slate-500">
                      {form.status} · accepting: {String(form.acceptingResponses)} · public: {String(form.publicAccess)}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => onTogglePublish(form)}
                    className="rounded-lg border px-3 py-2 text-sm hover:bg-slate-50"
                  >
                    {form.status === 'PUBLISHED' ? 'Unpublish' : 'Publish'}
                  </button>
                </div>

                <div className="flex flex-wrap gap-2 text-sm">
                  <Link href={`/dashboard/forms/${form.id}/edit`} className="rounded-lg border px-3 py-2 hover:bg-slate-50">
                    Builder
                  </Link>
                  <Link href={`/dashboard/forms/${form.id}/results`} className="rounded-lg border px-3 py-2 hover:bg-slate-50">
                    Results
                  </Link>
                  <Link href={publicUrl} className="rounded-lg border px-3 py-2 hover:bg-slate-50">
                    Public form
                  </Link>
                </div>
              </article>
            );
          })
        )}
      </div>
    </section>
  );
}
