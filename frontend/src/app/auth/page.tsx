'use client';

import { useRouter } from 'next/navigation';
import { FormEvent, useState } from 'react';
import { login, register } from '@/lib/api';

export default function AuthPage() {
  const router = useRouter();
  const [mode, setMode] = useState<'login' | 'register'>('register');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const auth = mode === 'register' ? await register(email, password, name) : await login(email, password);
      localStorage.setItem('forms.userId', String(auth.userId));
      localStorage.setItem('forms.token', auth.accessToken);
      localStorage.setItem('forms.email', auth.email);
      localStorage.setItem('forms.name', auth.name);
      router.push('/dashboard');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Request failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="mx-auto max-w-md rounded-xl border bg-white p-6 shadow-sm">
      <h1 className="mb-4 text-2xl font-semibold">{mode === 'register' ? 'Register' : 'Sign in'}</h1>
      <form className="space-y-3" onSubmit={onSubmit}>
        {mode === 'register' && (
          <label className="block">
            <span className="mb-1 block text-sm">Name</span>
            <input required className="w-full rounded-lg border px-3 py-2" value={name} onChange={(e) => setName(e.target.value)} />
          </label>
        )}

        <label className="block">
          <span className="mb-1 block text-sm">Email</span>
          <input
            required
            type="email"
            className="w-full rounded-lg border px-3 py-2"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </label>

        <label className="block">
          <span className="mb-1 block text-sm">Password</span>
          <input
            required
            minLength={8}
            type="password"
            className="w-full rounded-lg border px-3 py-2"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </label>

        {error && <p className="rounded-lg bg-red-50 p-2 text-sm text-red-700">{error}</p>}

        <button disabled={loading} className="w-full rounded-lg bg-indigo-600 px-4 py-2 text-white disabled:opacity-60" type="submit">
          {loading ? 'Please wait...' : mode === 'register' ? 'Create account' : 'Sign in'}
        </button>
      </form>

      <button type="button" onClick={() => setMode(mode === 'register' ? 'login' : 'register')} className="mt-3 text-sm text-indigo-700">
        {mode === 'register' ? 'Already have an account? Sign in' : 'Need an account? Register'}
      </button>
    </section>
  );
}
