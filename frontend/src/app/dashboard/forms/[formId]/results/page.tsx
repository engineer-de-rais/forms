'use client';

import { useEffect, useState } from 'react';
import {
  downloadResponsesCsv,
  FormResultsResponse,
  FormSubmissionDetailsResponse,
  FormSubmissionItemResponse,
  getFormResults,
  getFormSubmissionDetails,
  getFormSubmissions
} from '@/lib/api';
import { getStoredUserId } from '@/lib/session';

export default function FormResultsPage({ params }: { params: { formId: string } }) {
  const formId = Number(params.formId);

  const [userId, setUserId] = useState<number | null>(null);
  const [results, setResults] = useState<FormResultsResponse | null>(null);
  const [submissions, setSubmissions] = useState<FormSubmissionItemResponse[]>([]);
  const [selectedSubmission, setSelectedSubmission] = useState<FormSubmissionDetailsResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const resolvedUserId = getStoredUserId();
    if (!resolvedUserId) {
      setError('Open /auth first and sign in.');
      return;
    }
    setUserId(resolvedUserId);
    void loadAll(resolvedUserId, formId);
  }, [formId]);

  async function loadAll(resolvedUserId: number, resolvedFormId: number) {
    try {
      const [loadedResults, loadedSubmissions] = await Promise.all([
        getFormResults(resolvedUserId, resolvedFormId),
        getFormSubmissions(resolvedUserId, resolvedFormId)
      ]);
      setResults(loadedResults);
      setSubmissions(loadedSubmissions);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load results');
    }
  }

  async function onSelectSubmission(submissionId: number) {
    if (!userId) {
      return;
    }
    try {
      const details = await getFormSubmissionDetails(userId, formId, submissionId);
      setSelectedSubmission(details);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load submission');
    }
  }

  async function onDownloadCsv() {
    if (!userId) {
      return;
    }
    try {
      const blob = await downloadResponsesCsv(userId, formId);
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `form-${formId}-responses.csv`;
      link.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to download CSV');
    }
  }

  return (
    <section className="space-y-6">
      <div className="rounded-xl border bg-white p-6 shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h1 className="text-2xl font-semibold">Form Results</h1>
          <button type="button" className="rounded-lg border px-3 py-2 text-sm hover:bg-slate-50" onClick={onDownloadCsv}>
            Download CSV
          </button>
        </div>
        <p className="text-slate-600">Total responses: {results?.totalResponses ?? 0}</p>
      </div>

      {error && <p className="rounded-lg bg-red-50 p-3 text-red-700">{error}</p>}

      <div className="grid gap-6 lg:grid-cols-2">
        <article className="space-y-3 rounded-xl border bg-white p-6 shadow-sm">
          <h2 className="text-lg font-semibold">Aggregates</h2>
          {!results || results.questions.length === 0 ? (
            <p className="text-slate-600">No questions yet.</p>
          ) : (
            <ul className="space-y-4">
              {results.questions.map((question) => (
                <li key={question.questionId} className="space-y-2 rounded-lg border p-3">
                  <p className="font-medium">{question.title}</p>
                  <p className="text-xs text-slate-500">{question.type}</p>

                  {question.buckets.length > 0 ? (
                    <ul className="space-y-1 text-sm">
                      {question.buckets.map((bucket) => (
                        <li key={`${question.questionId}-${bucket.value}`} className="flex items-center justify-between">
                          <span>{bucket.value}</span>
                          <span className="font-medium">{bucket.count}</span>
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <ul className="space-y-1 text-sm text-slate-700">
                      {question.latestAnswers.length === 0 ? (
                        <li>No answers yet.</li>
                      ) : (
                        question.latestAnswers.map((answer, index) => <li key={`${question.questionId}-latest-${index}`}>{answer}</li>)
                      )}
                    </ul>
                  )}
                </li>
              ))}
            </ul>
          )}
        </article>

        <article className="space-y-3 rounded-xl border bg-white p-6 shadow-sm">
          <h2 className="text-lg font-semibold">Responses</h2>
          {submissions.length === 0 ? (
            <p className="text-slate-600">No responses yet.</p>
          ) : (
            <ul className="space-y-2">
              {submissions.map((submission) => (
                <li key={submission.id} className="flex items-center justify-between rounded-lg border p-3">
                  <div>
                    <p className="font-medium">#{submission.id}</p>
                    <p className="text-xs text-slate-500">{submission.submittedAt}</p>
                  </div>
                  <button
                    type="button"
                    className="rounded border px-3 py-2 text-sm hover:bg-slate-50"
                    onClick={() => onSelectSubmission(submission.id)}
                  >
                    Open
                  </button>
                </li>
              ))}
            </ul>
          )}

          {selectedSubmission && (
            <div className="space-y-2 rounded-lg border p-3">
              <p className="font-medium">Submission #{selectedSubmission.id}</p>
              <p className="text-xs text-slate-500">{selectedSubmission.submittedAt}</p>
              <ul className="space-y-2">
                {selectedSubmission.answers.map((answer) => (
                  <li key={`${selectedSubmission.id}-${answer.questionId}`} className="rounded border p-2">
                    <p className="text-sm font-medium">{answer.questionTitle}</p>
                    <p className="text-xs text-slate-500">{answer.questionType}</p>
                    <p className="text-sm">{answer.value || 'No answer'}</p>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </article>
      </div>
    </section>
  );
}
