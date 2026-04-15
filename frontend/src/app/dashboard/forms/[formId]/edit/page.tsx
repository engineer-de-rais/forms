'use client';

import Link from 'next/link';
import { useEffect, useMemo, useState } from 'react';
import {
  FormDetailsResponse,
  FormQuestionRequest,
  FormQuestionResponse,
  QuestionType,
  getFormStructure,
  publishForm,
  unpublishForm,
  updateFormStructure
} from '@/lib/api';
import { getStoredUserId } from '@/lib/session';

type EditableOption = {
  value: string;
  goToSection: string;
};

type EditableQuestion = {
  localId: string;
  section: number;
  title: string;
  description: string;
  type: QuestionType;
  required: boolean;
  options: EditableOption[];
  linearMin: number | null;
  linearMax: number | null;
  linearMinLabel: string;
  linearMaxLabel: string;
};

const QUESTION_TYPES: { label: string; value: QuestionType }[] = [
  { label: 'Short text', value: 'SHORT_TEXT' },
  { label: 'Paragraph', value: 'PARAGRAPH' },
  { label: 'Single choice', value: 'SINGLE_CHOICE' },
  { label: 'Multiple choice', value: 'MULTIPLE_CHOICE' },
  { label: 'Dropdown', value: 'DROPDOWN' },
  { label: 'Date', value: 'DATE' },
  { label: 'Time', value: 'TIME' },
  { label: 'Linear scale', value: 'LINEAR_SCALE' }
];

export default function FormBuilderPage({ params }: { params: { formId: string } }) {
  const formId = Number(params.formId);

  const [userId, setUserId] = useState<number | null>(null);
  const [form, setForm] = useState<FormDetailsResponse | null>(null);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [theme, setTheme] = useState('classic');
  const [acceptingResponses, setAcceptingResponses] = useState(true);
  const [publicAccess, setPublicAccess] = useState(true);
  const [questions, setQuestions] = useState<EditableQuestion[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [preview, setPreview] = useState(false);

  useEffect(() => {
    const resolvedUserId = getStoredUserId();
    if (!resolvedUserId) {
      setError('Open /auth first and sign in.');
      return;
    }
    setUserId(resolvedUserId);
    void loadForm(resolvedUserId, formId);
  }, [formId]);

  async function loadForm(resolvedUserId: number, resolvedFormId: number) {
    try {
      const details = await getFormStructure(resolvedUserId, resolvedFormId);
      hydrate(details);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load form');
    }
  }

  function hydrate(details: FormDetailsResponse) {
    setForm(details);
    setTitle(details.title);
    setDescription(details.description);
    setTheme(details.theme);
    setAcceptingResponses(details.acceptingResponses);
    setPublicAccess(details.publicAccess);
    setQuestions(details.questions.map(fromApiQuestion));
  }

  const publicLink = useMemo(() => (form ? `/f/${form.slug}` : '#'), [form]);

  function addQuestion(type: QuestionType) {
    setQuestions((prev) => [
      ...prev,
      {
        localId: makeLocalId(),
        section: prev.length === 0 ? 0 : prev[prev.length - 1].section,
        title: 'New question',
        description: '',
        type,
        required: false,
        options:
          type === 'SINGLE_CHOICE' || type === 'MULTIPLE_CHOICE' || type === 'DROPDOWN'
            ? [
                { value: 'Option 1', goToSection: '' },
                { value: 'Option 2', goToSection: '' }
              ]
            : [],
        linearMin: type === 'LINEAR_SCALE' ? 1 : null,
        linearMax: type === 'LINEAR_SCALE' ? 5 : null,
        linearMinLabel: '',
        linearMaxLabel: ''
      }
    ]);
  }

  function updateQuestion(localId: string, patch: Partial<EditableQuestion>) {
    setQuestions((prev) => prev.map((question) => (question.localId === localId ? { ...question, ...patch } : question)));
  }

  function removeQuestion(localId: string) {
    setQuestions((prev) => prev.filter((question) => question.localId !== localId));
  }

  function duplicateQuestion(localId: string) {
    setQuestions((prev) => {
      const index = prev.findIndex((question) => question.localId === localId);
      if (index === -1) {
        return prev;
      }
      const source = prev[index];
      const clone: EditableQuestion = {
        ...source,
        localId: makeLocalId(),
        title: `${source.title} (copy)`,
        options: source.options.map((option) => ({ ...option }))
      };
      const next = [...prev];
      next.splice(index + 1, 0, clone);
      return next;
    });
  }

  function moveQuestion(localId: string, direction: 'up' | 'down') {
    setQuestions((prev) => {
      const index = prev.findIndex((question) => question.localId === localId);
      if (index === -1) {
        return prev;
      }
      const target = direction === 'up' ? index - 1 : index + 1;
      if (target < 0 || target >= prev.length) {
        return prev;
      }
      const next = [...prev];
      [next[index], next[target]] = [next[target], next[index]];
      return next;
    });
  }

  function updateOption(localId: string, optionIndex: number, patch: Partial<EditableOption>) {
    setQuestions((prev) =>
      prev.map((question) => {
        if (question.localId !== localId) {
          return question;
        }
        const nextOptions = question.options.map((option, index) => (index === optionIndex ? { ...option, ...patch } : option));
        return { ...question, options: nextOptions };
      })
    );
  }

  function addOption(localId: string) {
    setQuestions((prev) =>
      prev.map((question) =>
        question.localId === localId
          ? {
              ...question,
              options: [...question.options, { value: `Option ${question.options.length + 1}`, goToSection: '' }]
            }
          : question
      )
    );
  }

  function removeOption(localId: string, optionIndex: number) {
    setQuestions((prev) =>
      prev.map((question) => {
        if (question.localId !== localId) {
          return question;
        }
        return { ...question, options: question.options.filter((_, index) => index !== optionIndex) };
      })
    );
  }

  async function onSave() {
    if (!userId || !form) {
      return;
    }

    setSaving(true);
    setError(null);
    try {
      const payload = {
        title,
        description,
        theme: theme || 'classic',
        acceptingResponses,
        publicAccess,
        questions: questions.map(toApiQuestion)
      };
      const saved = await updateFormStructure(userId, form.id, payload);
      hydrate(saved);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to save form structure');
    } finally {
      setSaving(false);
    }
  }

  async function onTogglePublish() {
    if (!userId || !form) {
      return;
    }
    try {
      const updated = form.status === 'PUBLISHED' ? await unpublishForm(userId, form.id) : await publishForm(userId, form.id);
      setForm((prev) => (prev ? { ...prev, status: updated.status } : prev));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to change publish status');
    }
  }

  return (
    <section className="space-y-6">
      <div className="rounded-xl border bg-white p-6 shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h1 className="text-2xl font-semibold">Form Builder</h1>
          <div className="flex flex-wrap gap-2">
            <button className="rounded-lg border px-3 py-2 text-sm hover:bg-slate-50" type="button" onClick={() => setPreview((value) => !value)}>
              {preview ? 'Hide preview' : 'Preview'}
            </button>
            <button className="rounded-lg border px-3 py-2 text-sm hover:bg-slate-50" type="button" onClick={onTogglePublish}>
              {form?.status === 'PUBLISHED' ? 'Unpublish' : 'Publish'}
            </button>
            <button className="rounded-lg bg-indigo-600 px-3 py-2 text-sm text-white disabled:opacity-60" type="button" onClick={onSave} disabled={saving}>
              {saving ? 'Saving...' : 'Save'}
            </button>
          </div>
        </div>
        {form && (
          <p className="mt-2 text-sm text-slate-600">
            Status: {form.status} · Public link:{' '}
            <Link className="text-indigo-700 underline" href={publicLink}>
              {publicLink}
            </Link>
          </p>
        )}
      </div>

      {error && <p className="rounded-lg bg-red-50 p-3 text-red-700">{error}</p>}

      <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
        <div className="space-y-4">
          <article className="space-y-3 rounded-xl border bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold">Form settings</h2>
            <label className="block space-y-1">
              <span className="text-sm">Title</span>
              <input className="w-full rounded-lg border px-3 py-2" value={title} onChange={(e) => setTitle(e.target.value)} />
            </label>
            <label className="block space-y-1">
              <span className="text-sm">Description</span>
              <textarea className="w-full rounded-lg border px-3 py-2" value={description} onChange={(e) => setDescription(e.target.value)} />
            </label>
            <label className="block space-y-1">
              <span className="text-sm">Theme</span>
              <input className="w-full rounded-lg border px-3 py-2" value={theme} onChange={(e) => setTheme(e.target.value)} />
            </label>
            <label className="flex items-center gap-2 text-sm">
              <input type="checkbox" checked={acceptingResponses} onChange={(e) => setAcceptingResponses(e.target.checked)} />
              Accepting responses
            </label>
            <label className="flex items-center gap-2 text-sm">
              <input type="checkbox" checked={publicAccess} onChange={(e) => setPublicAccess(e.target.checked)} />
              Public access enabled
            </label>
          </article>

          <div className="space-y-3">
            {questions.map((question, index) => {
              const isChoice = question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE' || question.type === 'DROPDOWN';
              const branchingEnabled = question.type === 'SINGLE_CHOICE' || question.type === 'DROPDOWN';
              const isScale = question.type === 'LINEAR_SCALE';

              return (
                <article key={question.localId} className="space-y-3 rounded-xl border bg-white p-6 shadow-sm">
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <h3 className="font-semibold">Question {index + 1}</h3>
                    <div className="flex gap-2 text-xs">
                      <button type="button" className="rounded border px-2 py-1" onClick={() => moveQuestion(question.localId, 'up')}>
                        Up
                      </button>
                      <button type="button" className="rounded border px-2 py-1" onClick={() => moveQuestion(question.localId, 'down')}>
                        Down
                      </button>
                      <button type="button" className="rounded border px-2 py-1" onClick={() => duplicateQuestion(question.localId)}>
                        Duplicate
                      </button>
                      <button type="button" className="rounded border px-2 py-1 text-red-700" onClick={() => removeQuestion(question.localId)}>
                        Delete
                      </button>
                    </div>
                  </div>

                  <label className="block space-y-1">
                    <span className="text-sm">Section index</span>
                    <input
                      type="number"
                      min={0}
                      className="w-full rounded-lg border px-3 py-2"
                      value={question.section}
                      onChange={(e) => updateQuestion(question.localId, { section: Math.max(0, Number(e.target.value)) })}
                    />
                  </label>

                  <label className="block space-y-1">
                    <span className="text-sm">Type</span>
                    <select
                      value={question.type}
                      onChange={(e) => {
                        const nextType = e.target.value as QuestionType;
                        updateQuestion(question.localId, {
                          type: nextType,
                          options:
                            nextType === 'SINGLE_CHOICE' || nextType === 'MULTIPLE_CHOICE' || nextType === 'DROPDOWN'
                              ? question.options.length > 0
                                ? question.options
                                : [
                                    { value: 'Option 1', goToSection: '' },
                                    { value: 'Option 2', goToSection: '' }
                                  ]
                              : [],
                          linearMin: nextType === 'LINEAR_SCALE' ? question.linearMin ?? 1 : null,
                          linearMax: nextType === 'LINEAR_SCALE' ? question.linearMax ?? 5 : null
                        });
                      }}
                      className="w-full rounded-lg border px-3 py-2"
                    >
                      {QUESTION_TYPES.map((entry) => (
                        <option key={entry.value} value={entry.value}>
                          {entry.label}
                        </option>
                      ))}
                    </select>
                  </label>

                  <label className="block space-y-1">
                    <span className="text-sm">Title</span>
                    <input
                      className="w-full rounded-lg border px-3 py-2"
                      value={question.title}
                      onChange={(e) => updateQuestion(question.localId, { title: e.target.value })}
                    />
                  </label>

                  <label className="block space-y-1">
                    <span className="text-sm">Description</span>
                    <input
                      className="w-full rounded-lg border px-3 py-2"
                      value={question.description}
                      onChange={(e) => updateQuestion(question.localId, { description: e.target.value })}
                    />
                  </label>

                  <label className="flex items-center gap-2 text-sm">
                    <input
                      type="checkbox"
                      checked={question.required}
                      onChange={(e) => updateQuestion(question.localId, { required: e.target.checked })}
                    />
                    Required
                  </label>

                  {isChoice && (
                    <div className="space-y-2 rounded-lg border p-3">
                      <p className="text-sm font-medium">Options</p>
                      {question.options.map((option, optionIndex) => (
                        <div key={`${question.localId}-option-${optionIndex}`} className="grid gap-2 md:grid-cols-[2fr_1fr_auto]">
                          <input
                            className="rounded-lg border px-3 py-2"
                            value={option.value}
                            onChange={(e) => updateOption(question.localId, optionIndex, { value: e.target.value })}
                          />
                          {branchingEnabled ? (
                            <input
                              type="number"
                              min={0}
                              className="rounded-lg border px-3 py-2"
                              placeholder="Go to section (optional)"
                              value={option.goToSection}
                              onChange={(e) => updateOption(question.localId, optionIndex, { goToSection: e.target.value })}
                            />
                          ) : (
                            <div className="rounded-lg border bg-slate-50 px-3 py-2 text-sm text-slate-500">No branching for this type</div>
                          )}
                          <button type="button" className="rounded border px-3 py-2 text-sm" onClick={() => removeOption(question.localId, optionIndex)}>
                            Remove
                          </button>
                        </div>
                      ))}
                      <button type="button" className="rounded border px-3 py-2 text-sm" onClick={() => addOption(question.localId)}>
                        Add option
                      </button>
                    </div>
                  )}

                  {isScale && (
                    <div className="grid gap-2 md:grid-cols-2">
                      <label className="space-y-1">
                        <span className="text-sm">Min</span>
                        <input
                          type="number"
                          className="w-full rounded-lg border px-3 py-2"
                          value={question.linearMin ?? 1}
                          onChange={(e) => updateQuestion(question.localId, { linearMin: Number(e.target.value) })}
                        />
                      </label>
                      <label className="space-y-1">
                        <span className="text-sm">Max</span>
                        <input
                          type="number"
                          className="w-full rounded-lg border px-3 py-2"
                          value={question.linearMax ?? 5}
                          onChange={(e) => updateQuestion(question.localId, { linearMax: Number(e.target.value) })}
                        />
                      </label>
                      <label className="space-y-1">
                        <span className="text-sm">Min label</span>
                        <input
                          className="w-full rounded-lg border px-3 py-2"
                          value={question.linearMinLabel}
                          onChange={(e) => updateQuestion(question.localId, { linearMinLabel: e.target.value })}
                        />
                      </label>
                      <label className="space-y-1">
                        <span className="text-sm">Max label</span>
                        <input
                          className="w-full rounded-lg border px-3 py-2"
                          value={question.linearMaxLabel}
                          onChange={(e) => updateQuestion(question.localId, { linearMaxLabel: e.target.value })}
                        />
                      </label>
                    </div>
                  )}
                </article>
              );
            })}
          </div>
        </div>

        <aside className="space-y-3">
          <article className="rounded-xl border bg-white p-4 shadow-sm">
            <h2 className="mb-2 text-sm font-semibold">Add question</h2>
            <div className="grid gap-2">
              {QUESTION_TYPES.map((entry) => (
                <button key={entry.value} type="button" className="rounded border px-3 py-2 text-left text-sm hover:bg-slate-50" onClick={() => addQuestion(entry.value)}>
                  {entry.label}
                </button>
              ))}
            </div>
          </article>
        </aside>
      </div>

      {preview && (
        <article className="space-y-3 rounded-xl border bg-white p-6 shadow-sm">
          <h2 className="text-lg font-semibold">Preview</h2>
          <p className="text-slate-600">{description || 'No description'}</p>
          <ul className="space-y-2">
            {questions.map((question, index) => (
              <li key={`${question.localId}-preview`} className="rounded-lg border p-3">
                <p className="font-medium">
                  {index + 1}. {question.title}
                </p>
                <p className="text-xs text-slate-500">
                  section {question.section} · {question.type}
                  {question.required ? ' · required' : ''}
                </p>
              </li>
            ))}
          </ul>
        </article>
      )}
    </section>
  );
}

function makeLocalId() {
  return Math.random().toString(36).slice(2, 10);
}

function fromApiQuestion(question: FormQuestionResponse): EditableQuestion {
  return {
    localId: `q-${question.id}`,
    section: question.section ?? 0,
    title: question.title,
    description: question.description ?? '',
    type: question.type,
    required: question.required,
    options: question.options.map((option) => ({
      value: option.value,
      goToSection: option.goToSection == null ? '' : String(option.goToSection)
    })),
    linearMin: question.linearMin,
    linearMax: question.linearMax,
    linearMinLabel: question.linearMinLabel ?? '',
    linearMaxLabel: question.linearMaxLabel ?? ''
  };
}

function toApiQuestion(question: EditableQuestion): FormQuestionRequest {
  const isBranchingType = question.type === 'SINGLE_CHOICE' || question.type === 'DROPDOWN';

  return {
    section: Math.max(0, question.section),
    title: question.title,
    description: question.description,
    type: question.type,
    required: question.required,
    options: question.options.map((option) => ({
      value: option.value,
      goToSection: isBranchingType && option.goToSection !== '' ? Number(option.goToSection) : null
    })),
    linearMin: question.type === 'LINEAR_SCALE' ? question.linearMin : null,
    linearMax: question.type === 'LINEAR_SCALE' ? question.linearMax : null,
    linearMinLabel: question.type === 'LINEAR_SCALE' ? question.linearMinLabel || null : null,
    linearMaxLabel: question.type === 'LINEAR_SCALE' ? question.linearMaxLabel || null : null
  };
}
