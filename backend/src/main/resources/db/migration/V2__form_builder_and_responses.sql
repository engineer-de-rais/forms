ALTER TABLE forms
    ADD COLUMN theme VARCHAR(64) NOT NULL DEFAULT 'classic',
    ADD COLUMN accepting_responses BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN public_access BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE form_questions (
    id BIGSERIAL PRIMARY KEY,
    form_id BIGINT NOT NULL REFERENCES forms(id) ON DELETE CASCADE,
    position INT NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    type VARCHAR(64) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    linear_min INT NULL,
    linear_max INT NULL,
    linear_min_label VARCHAR(255) NULL,
    linear_max_label VARCHAR(255) NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_form_questions_form_position ON form_questions(form_id, position);

CREATE TABLE form_question_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES form_questions(id) ON DELETE CASCADE,
    position INT NOT NULL,
    value VARCHAR(500) NOT NULL
);

CREATE INDEX idx_form_question_options_question_position ON form_question_options(question_id, position);

CREATE TABLE form_submissions (
    id BIGSERIAL PRIMARY KEY,
    form_id BIGINT NOT NULL REFERENCES forms(id) ON DELETE CASCADE,
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_form_submissions_form_submitted ON form_submissions(form_id, submitted_at DESC);

CREATE TABLE form_submission_answers (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL REFERENCES form_submissions(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES form_questions(id),
    value_text TEXT NOT NULL
);

CREATE INDEX idx_form_submission_answers_submission ON form_submission_answers(submission_id);
CREATE INDEX idx_form_submission_answers_question ON form_submission_answers(question_id);
