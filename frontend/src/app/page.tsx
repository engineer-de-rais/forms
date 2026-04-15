import Link from 'next/link';

export default function HomePage() {
  return (
    <section className="space-y-6 rounded-xl border bg-white p-8 shadow-sm">
      <h1 className="text-3xl font-semibold">Конструктор форм (MVP)</h1>
      <p className="text-slate-600">
        Уже можно зарегистрироваться, войти, создать форму и посмотреть список своих форм.
      </p>
      <div className="flex gap-3">
        <Link href="/auth" className="rounded-lg bg-indigo-600 px-4 py-2 text-white hover:bg-indigo-500">
          Начать
        </Link>
        <Link href="/dashboard" className="rounded-lg border px-4 py-2 hover:bg-slate-50">
          Перейти в dashboard
        </Link>
      </div>
    </section>
  );
}
