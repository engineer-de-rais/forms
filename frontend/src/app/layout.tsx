import './globals.css';
import type { Metadata } from 'next';
import Link from 'next/link';
import { ReactNode } from 'react';

export const metadata: Metadata = {
  title: 'Forms MVP',
  description: 'Google Forms-like MVP'
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="ru">
      <body>
        <header className="border-b bg-white">
          <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
            <Link href="/" className="text-lg font-semibold text-indigo-700">
              Forms MVP
            </Link>
            <nav className="flex gap-4 text-sm">
              <Link href="/auth" className="hover:text-indigo-700">
                Вход
              </Link>
              <Link href="/dashboard" className="hover:text-indigo-700">
                Dashboard
              </Link>
            </nav>
          </div>
        </header>
        <main className="mx-auto max-w-5xl px-4 py-6">{children}</main>
      </body>
    </html>
  );
}
