ALTER TABLE form_questions
    ADD COLUMN section INT NOT NULL DEFAULT 0;

ALTER TABLE form_question_options
    ADD COLUMN go_to_section INT NULL;
