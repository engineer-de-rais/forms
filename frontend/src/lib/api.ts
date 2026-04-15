export type AuthResponse = {
  accessToken: string;
  userId: number;
  email: string;
  name: string;
};

export type FormResponse = {
  id: number;
  title: string;
  description: string;
  status: 'DRAFT' | 'PUBLISHED';
  slug: string;
  createdAt: string;
  updatedAt: string;
};

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

async function parseJson<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const payload = await response.json().catch(() => ({ error: 'Request failed' }));
    throw new Error(payload.error ?? 'Request failed');
  }

  return response.json() as Promise<T>;
}

export async function register(email: string, password: string, name: string): Promise<AuthResponse> {
  const response = await fetch(`${API_URL}/api/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, name })
  });

  return parseJson<AuthResponse>(response);
}

export async function login(email: string, password: string): Promise<AuthResponse> {
  const response = await fetch(`${API_URL}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });

  return parseJson<AuthResponse>(response);
}

export async function getForms(userId: number): Promise<FormResponse[]> {
  const response = await fetch(`${API_URL}/api/forms`, {
    headers: { 'X-User-Id': String(userId) },
    cache: 'no-store'
  });

  return parseJson<FormResponse[]>(response);
}

export async function createForm(userId: number, title: string, description: string): Promise<FormResponse> {
  const response = await fetch(`${API_URL}/api/forms`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId)
    },
    body: JSON.stringify({ title, description })
  });

  return parseJson<FormResponse>(response);
}
