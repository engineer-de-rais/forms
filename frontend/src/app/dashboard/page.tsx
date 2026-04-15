'use client';

import { FormEvent, useEffect, useState } from 'react';
import { createForm, FormResponse, getForms } from '@/lib/api';

export default function DashboardPage() {
  const [userId, setUserId] = useState<number | null>(null);
  const [forms, setForms] = useState<FormResponse[]>([]);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const raw = localStorage.getItem('forms.userId');
    if (!raw) {
      setError('Сначала выполните вход или регистрацию на странице /auth');
      return;
    }

    const parsed = Number(raw);
    setUserId(parsed);

    getForms(parsed)
      .then(setForms)
      .catch((e) => setError(e instanceof Error ? e.message : 'Не удалось загрузить формы'));
  }, []);

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
      setError(e instanceof Error ? e.message : 'Не удалось создать форму');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="space-y-6">
      <div className="rounded-xl border bg-white p-6 shadow-sm">
        <h1 className="text-2xl font-semibold">Dashboard</h1>
        <p className="text-slate-600">Создайте форму и получите список своих форм.</p>
      </div>

      <form onSubmit={onCreate} className="space-y-3 rounded-xl border bg-white p-6 shadow-sm">
        <h2 className="text-lg font-semibold">Новая форма</h2>
        <input
          required
          className="w-full rounded-lg border px-3 py-2"
          placeholder="Название формы"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
        <textarea
          className="w-full rounded-lg border px-3 py-2"
          placeholder="Описание"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
        <button className="rounded-lg bg-indigo-600 px-4 py-2 text-white disabled:opacity-60" disabled={loading}>
          {loading ? 'Создаём…' : 'Создать форму'}
        </button>
      </form>

      {error && <p className="rounded-lg bg-red-50 p-3 text-red-700">{error}</p>}

      <div className="rounded-xl border bg-white p-6 shadow-sm">
        <h2 className="mb-3 text-lg font-semibold">Мои формы</h2>
        {forms.length === 0 ? (
          <p className="text-slate-600">Пока нет форм.</p>
        ) : (
          <ul className="space-y-2">
            {forms.map((form) => (
              <li key={form.id} className="rounded-lg border p-3">
                <p className="font-medium">{form.title}</p>
                <p className="text-sm text-slate-600">{form.description || 'Без описания'}</p>
                <p className="text-xs text-slate-500">
                  Статус: {form.status} · Slug: {form.slug}
                </p>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  );
}
