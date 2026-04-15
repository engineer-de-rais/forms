export type AuthResponse = {
  accessToken: string;
  userId: number;
  email: string;
  name: string;
};

export type FormStatus = 'DRAFT' | 'PUBLISHED';

export type QuestionType =
  | 'SHORT_TEXT'
  | 'PARAGRAPH'
  | 'SINGLE_CHOICE'
  | 'MULTIPLE_CHOICE'
  | 'DROPDOWN'
  | 'DATE'
  | 'TIME'
  | 'LINEAR_SCALE';

export type FormResponse = {
  id: number;
  title: string;
  description: string;
  status: FormStatus;
  slug: string;
  theme: string;
  acceptingResponses: boolean;
  publicAccess: boolean;
  createdAt: string;
  updatedAt: string;
};

export type FormQuestionOptionResponse = {
  id: number;
  position: number;
  value: string;
  goToSection: number | null;
};

export type FormQuestionResponse = {
  id: number;
  position: number;
  section: number;
  title: string;
  description: string;
  type: QuestionType;
  required: boolean;
  options: FormQuestionOptionResponse[];
  linearMin: number | null;
  linearMax: number | null;
  linearMinLabel: string | null;
  linearMaxLabel: string | null;
};

export type FormDetailsResponse = {
  id: number;
  title: string;
  description: string;
  status: FormStatus;
  slug: string;
  theme: string;
  acceptingResponses: boolean;
  publicAccess: boolean;
  createdAt: string;
  updatedAt: string;
  questions: FormQuestionResponse[];
};

export type PublicFormResponse = {
  id: number;
  title: string;
  description: string;
  slug: string;
  status: FormStatus;
  theme: string;
  acceptingResponses: boolean;
  questions: FormQuestionResponse[];
};

export type FormSubmissionItemResponse = {
  id: number;
  submittedAt: string;
};

export type FormSubmissionAnswerResponse = {
  questionId: number;
  questionTitle: string;
  questionType: QuestionType;
  value: string;
};

export type FormSubmissionDetailsResponse = {
  id: number;
  submittedAt: string;
  answers: FormSubmissionAnswerResponse[];
};

export type QuestionBucketResponse = {
  value: string;
  count: number;
};

export type QuestionResultsResponse = {
  questionId: number;
  title: string;
  type: QuestionType;
  buckets: QuestionBucketResponse[];
  latestAnswers: string[];
};

export type FormResultsResponse = {
  formId: number;
  totalResponses: number;
  questions: QuestionResultsResponse[];
};

export type FormQuestionOptionRequest = {
  value: string;
  goToSection?: number | null;
};

export type FormQuestionRequest = {
  section: number;
  title: string;
  description: string;
  type: QuestionType;
  required: boolean;
  options: FormQuestionOptionRequest[];
  linearMin: number | null;
  linearMax: number | null;
  linearMinLabel: string | null;
  linearMaxLabel: string | null;
};

export type UpdateFormStructureRequest = {
  title: string;
  description: string;
  theme: string;
  acceptingResponses: boolean;
  publicAccess: boolean;
  questions: FormQuestionRequest[];
};

export type SubmitFormAnswerRequest = {
  questionId: number;
  value?: string;
  values?: string[];
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

export async function publishForm(userId: number, formId: number): Promise<FormResponse> {
  const response = await fetch(`${API_URL}/api/forms/${formId}/publish`, {
    method: 'POST',
    headers: { 'X-User-Id': String(userId) }
  });

  return parseJson<FormResponse>(response);
}

export async function unpublishForm(userId: number, formId: number): Promise<FormResponse> {
  const response = await fetch(`${API_URL}/api/forms/${formId}/unpublish`, {
    method: 'POST',
    headers: { 'X-User-Id': String(userId) }
  });

  return parseJson<FormResponse>(response);
}

export async function getFormStructure(userId: number, formId: number): Promise<FormDetailsResponse> {
  const response = await fetch(`${API_URL}/api/forms/${formId}/structure`, {
    headers: { 'X-User-Id': String(userId) },
    cache: 'no-store'
  });
  return parseJson<FormDetailsResponse>(response);
}

export async function updateFormStructure(
  userId: number,
  formId: number,
  request: UpdateFormStructureRequest
): Promise<FormDetailsResponse> {
  const response = await fetch(`${API_URL}/api/forms/${formId}/structure`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': String(userId)
    },
    body: JSON.stringify(request)
  });
  return parseJson<FormDetailsResponse>(response);
}

export async function getPublicForm(slug: string): Promise<PublicFormResponse> {
  const response = await fetch(`${API_URL}/api/public/forms/${slug}`, {
    cache: 'no-store'
  });
  return parseJson<PublicFormResponse>(response);
}

export async function submitPublicForm(
  slug: string,
  answers: SubmitFormAnswerRequest[]
): Promise<{ submissionId: number; submittedAt: string }> {
  const response = await fetch(`${API_URL}/api/public/forms/${slug}/responses`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ answers })
  });
  return parseJson<{ submissionId: number; submittedAt: string }>(response);
}

export async function getFormResults(userId: number, formId: number): Promise<FormResultsResponse> {
  const response = await fetch(`${API_URL}/api/forms/${formId}/results`, {
    headers: { 'X-User-Id': String(userId) },
    cache: 'no-store'
  });
  return parseJson<FormResultsResponse>(response);
}

export async function getFormSubmissions(userId: number, formId: number): Promise<FormSubmissionItemResponse[]> {
  const response = await fetch(`${API_URL}/api/forms/${formId}/responses`, {
    headers: { 'X-User-Id': String(userId) },
    cache: 'no-store'
  });
  return parseJson<FormSubmissionItemResponse[]>(response);
}

export async function getFormSubmissionDetails(
  userId: number,
  formId: number,
  submissionId: number
): Promise<FormSubmissionDetailsResponse> {
  const response = await fetch(`${API_URL}/api/forms/${formId}/responses/${submissionId}`, {
    headers: { 'X-User-Id': String(userId) },
    cache: 'no-store'
  });
  return parseJson<FormSubmissionDetailsResponse>(response);
}

export async function downloadResponsesCsv(userId: number, formId: number): Promise<Blob> {
  const response = await fetch(`${API_URL}/api/forms/${formId}/responses.csv`, {
    headers: { 'X-User-Id': String(userId) }
  });
  if (!response.ok) {
    const payload = await response.json().catch(() => ({ error: 'Failed to download CSV' }));
    throw new Error(payload.error ?? 'Failed to download CSV');
  }
  return response.blob();
}
