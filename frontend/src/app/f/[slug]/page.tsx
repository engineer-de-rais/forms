'use client';

import { Dispatch, SetStateAction, useEffect, useMemo, useState } from 'react';
import { FormQuestionResponse, getPublicForm, PublicFormResponse, submitPublicForm, SubmitFormAnswerRequest } from '@/lib/api';

type AnswerState = Record<number, string | string[]>;

export default function PublicFormPage({ params }: { params: { slug: string } }) {
  const [form, setForm] = useState<PublicFormResponse | null>(null);
  const [answers, setAnswers] = useState<AnswerState>({});
  const [currentSection, setCurrentSection] = useState(0);
  const [submitted, setSubmitted] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    void loadForm(params.slug);
  }, [params.slug]);

  async function loadForm(slug: string) {
    try {
      const loaded = await getPublicForm(slug);
      setForm(loaded);
      const firstSection = getSections(loaded.questions)[0] ?? 0;
      setCurrentSection(firstSection);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load form');
    }
  }

  const orderedQuestions = useMemo(
    () => (form ? [...form.questions].sort((left, right) => left.section - right.section || left.position - right.position) : []),
    [form]
  );
  const sections = useMemo(() => getSections(orderedQuestions), [orderedQuestions]);
  const currentSectionIndex = sections.findIndex((section) => section === currentSection);
  const visibleQuestions = orderedQuestions.filter((question) => question.section === currentSection);

  const disabled = useMemo(() => !form || !form.acceptingResponses || submitted, [form, submitted]);

  async function onNextOrSubmit() {
    if (!form) {
      return;
    }

    const missingRequired = visibleQuestions.find((question) => question.required && !hasAnswer(question, answers[question.id]));
    if (missingRequired) {
      setError(`Required question is missing: ${missingRequired.title}`);
      return;
    }

    const branchTarget = resolveBranchTarget(visibleQuestions, answers);
    const nextSection = branchTarget ?? sections[currentSectionIndex + 1];
    if (nextSection != null) {
      setError(null);
      setCurrentSection(nextSection);
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      const payload = buildSubmitPayload(answers);
      await submitPublicForm(form.slug, payload);
      setSubmitted(true);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to submit form');
    } finally {
      setSubmitting(false);
    }
  }

  function onBack() {
    const previousSection = sections[currentSectionIndex - 1];
    if (previousSection != null) {
      setCurrentSection(previousSection);
    }
  }

  if (submitted) {
    return (
      <section className="mx-auto max-w-3xl rounded-xl border bg-white p-8 shadow-sm">
        <h1 className="text-2xl font-semibold">Thanks for your response</h1>
      </section>
    );
  }

  return (
    <section className="mx-auto max-w-3xl space-y-4">
      {error && <p className="rounded-lg bg-red-50 p-3 text-red-700">{error}</p>}
      {!form ? (
        <p className="rounded-xl border bg-white p-6 shadow-sm">Loading...</p>
      ) : (
        <>
          <article className="rounded-xl border bg-white p-6 shadow-sm">
            <h1 className="text-2xl font-semibold">{form.title}</h1>
            <p className="text-slate-600">{form.description}</p>
            <p className="mt-2 text-xs text-slate-500">
              Section {currentSectionIndex + 1} of {Math.max(1, sections.length)}
            </p>
            {!form.acceptingResponses && <p className="mt-2 text-sm text-red-700">This form is no longer accepting responses.</p>}
          </article>

          <div className="space-y-3">
            {visibleQuestions.map((question, index) => (
              <article key={question.id} className="space-y-2 rounded-xl border bg-white p-6 shadow-sm">
                <p className="font-medium">
                  {index + 1}. {question.title} {question.required && <span className="text-red-600">*</span>}
                </p>
                {question.description && <p className="text-sm text-slate-600">{question.description}</p>}
                <QuestionInput question={question} answers={answers} setAnswers={setAnswers} disabled={disabled} />
              </article>
            ))}
          </div>

          <div className="flex flex-wrap gap-2">
            <button type="button" disabled={disabled || currentSectionIndex <= 0} onClick={onBack} className="rounded-lg border px-4 py-2 disabled:opacity-60">
              Back
            </button>
            <button
              type="button"
              disabled={disabled || submitting}
              onClick={onNextOrSubmit}
              className="rounded-lg bg-indigo-600 px-4 py-2 text-white disabled:opacity-60"
            >
              {submitting ? 'Submitting...' : currentSectionIndex === sections.length - 1 ? 'Submit' : 'Next'}
            </button>
          </div>
        </>
      )}
    </section>
  );
}

function QuestionInput({
  question,
  answers,
  setAnswers,
  disabled
}: {
  question: FormQuestionResponse;
  answers: AnswerState;
  setAnswers: Dispatch<SetStateAction<AnswerState>>;
  disabled: boolean;
}) {
  const value = answers[question.id];
  const isChoice = question.type === 'SINGLE_CHOICE' || question.type === 'DROPDOWN';
  const isMulti = question.type === 'MULTIPLE_CHOICE';

  if (question.type === 'PARAGRAPH') {
    return (
      <textarea
        className="w-full rounded-lg border px-3 py-2"
        value={typeof value === 'string' ? value : ''}
        onChange={(e) => setAnswers((prev) => ({ ...prev, [question.id]: e.target.value }))}
        disabled={disabled}
      />
    );
  }

  if (question.type === 'SHORT_TEXT') {
    return (
      <input
        className="w-full rounded-lg border px-3 py-2"
        value={typeof value === 'string' ? value : ''}
        onChange={(e) => setAnswers((prev) => ({ ...prev, [question.id]: e.target.value }))}
        disabled={disabled}
      />
    );
  }

  if (isChoice) {
    return (
      <select
        className="w-full rounded-lg border px-3 py-2"
        value={typeof value === 'string' ? value : ''}
        onChange={(e) => setAnswers((prev) => ({ ...prev, [question.id]: e.target.value }))}
        disabled={disabled}
      >
        <option value="">Select...</option>
        {question.options.map((option) => (
          <option key={option.id} value={option.value}>
            {option.value}
          </option>
        ))}
      </select>
    );
  }

  if (isMulti) {
    const selected = Array.isArray(value) ? value : [];
    return (
      <div className="space-y-2">
        {question.options.map((option) => (
          <label key={option.id} className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={selected.includes(option.value)}
              disabled={disabled}
              onChange={(e) =>
                setAnswers((prev) => {
                  const prevSelected = Array.isArray(prev[question.id]) ? (prev[question.id] as string[]) : [];
                  const nextSelected = e.target.checked
                    ? [...prevSelected, option.value]
                    : prevSelected.filter((item) => item !== option.value);
                  return { ...prev, [question.id]: nextSelected };
                })
              }
            />
            {option.value}
          </label>
        ))}
      </div>
    );
  }

  if (question.type === 'DATE') {
    return (
      <input
        type="date"
        className="w-full rounded-lg border px-3 py-2"
        value={typeof value === 'string' ? value : ''}
        onChange={(e) => setAnswers((prev) => ({ ...prev, [question.id]: e.target.value }))}
        disabled={disabled}
      />
    );
  }

  if (question.type === 'TIME') {
    return (
      <input
        type="time"
        className="w-full rounded-lg border px-3 py-2"
        value={typeof value === 'string' ? value : ''}
        onChange={(e) => setAnswers((prev) => ({ ...prev, [question.id]: e.target.value }))}
        disabled={disabled}
      />
    );
  }

  if (question.type === 'LINEAR_SCALE') {
    const min = question.linearMin ?? 1;
    const max = question.linearMax ?? 5;
    const sliderValue = typeof value === 'string' ? value : String(min);
    return (
      <div className="space-y-2">
        <input
          type="range"
          min={min}
          max={max}
          value={sliderValue}
          onChange={(e) => setAnswers((prev) => ({ ...prev, [question.id]: e.target.value }))}
          disabled={disabled}
          className="w-full"
        />
        <div className="flex items-center justify-between text-xs text-slate-600">
          <span>{question.linearMinLabel || min}</span>
          <span>{sliderValue}</span>
          <span>{question.linearMaxLabel || max}</span>
        </div>
      </div>
    );
  }

  return null;
}

function getSections(questions: FormQuestionResponse[]) {
  const unique = new Set<number>();
  for (const question of questions) {
    unique.add(question.section ?? 0);
  }
  return [...unique].sort((left, right) => left - right);
}

function hasAnswer(question: FormQuestionResponse, answer: string | string[] | undefined) {
  if (answer == null) {
    return false;
  }
  if (Array.isArray(answer)) {
    return answer.length > 0;
  }
  return answer.trim().length > 0;
}

function resolveBranchTarget(questions: FormQuestionResponse[], answers: AnswerState) {
  for (const question of questions) {
    if (question.type !== 'SINGLE_CHOICE' && question.type !== 'DROPDOWN') {
      continue;
    }
    const value = answers[question.id];
    if (typeof value !== 'string' || !value.trim()) {
      continue;
    }
    const selected = question.options.find((option) => option.value === value.trim());
    if (selected?.goToSection != null) {
      return selected.goToSection;
    }
  }
  return null;
}

function buildSubmitPayload(answers: AnswerState): SubmitFormAnswerRequest[] {
  const payload: SubmitFormAnswerRequest[] = [];
  for (const [questionIdKey, answerValue] of Object.entries(answers)) {
    const questionId = Number(questionIdKey);
    if (Array.isArray(answerValue)) {
      if (answerValue.length > 0) {
        payload.push({ questionId, values: answerValue });
      }
      continue;
    }
    if (answerValue.trim().length > 0) {
      payload.push({ questionId, value: answerValue });
    }
  }
  return payload;
}
