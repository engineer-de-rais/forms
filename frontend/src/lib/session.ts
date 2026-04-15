export function getStoredUserId(): number | null {
  if (typeof window === 'undefined') {
    return null;
  }
  const raw = localStorage.getItem('forms.userId');
  if (!raw) {
    return null;
  }
  const parsed = Number(raw);
  return Number.isFinite(parsed) ? parsed : null;
}
