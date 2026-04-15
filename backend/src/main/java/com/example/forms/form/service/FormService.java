package com.example.forms.form.service;

import com.example.forms.auth.entity.User;
import com.example.forms.auth.repository.UserRepository;
import com.example.forms.common.exception.NotFoundException;
import com.example.forms.form.dto.CreateFormRequest;
import com.example.forms.form.dto.FormDetailsResponse;
import com.example.forms.form.dto.FormQuestionOptionRequest;
import com.example.forms.form.dto.FormQuestionOptionResponse;
import com.example.forms.form.dto.FormQuestionRequest;
import com.example.forms.form.dto.FormQuestionResponse;
import com.example.forms.form.dto.FormResponse;
import com.example.forms.form.dto.FormResultsResponse;
import com.example.forms.form.dto.FormSubmissionAnswerResponse;
import com.example.forms.form.dto.FormSubmissionDetailsResponse;
import com.example.forms.form.dto.FormSubmissionItemResponse;
import com.example.forms.form.dto.PublicFormResponse;
import com.example.forms.form.dto.QuestionBucketResponse;
import com.example.forms.form.dto.QuestionResultsResponse;
import com.example.forms.form.dto.SubmitFormAnswerRequest;
import com.example.forms.form.dto.SubmitFormRequest;
import com.example.forms.form.dto.SubmitFormResponse;
import com.example.forms.form.dto.UpdateFormRequest;
import com.example.forms.form.dto.UpdateFormStructureRequest;
import com.example.forms.form.entity.Form;
import com.example.forms.form.entity.FormQuestion;
import com.example.forms.form.entity.FormQuestionOption;
import com.example.forms.form.entity.FormStatus;
import com.example.forms.form.entity.FormSubmission;
import com.example.forms.form.entity.FormSubmissionAnswer;
import com.example.forms.form.entity.QuestionType;
import com.example.forms.form.repository.FormQuestionRepository;
import com.example.forms.form.repository.FormRepository;
import com.example.forms.form.repository.FormSubmissionAnswerRepository;
import com.example.forms.form.repository.FormSubmissionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FormService {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final FormRepository formRepository;
    private final UserRepository userRepository;
    private final FormQuestionRepository formQuestionRepository;
    private final FormSubmissionRepository formSubmissionRepository;
    private final FormSubmissionAnswerRepository formSubmissionAnswerRepository;

    @Transactional
    public FormResponse create(Long userId, CreateFormRequest request) {
        User owner = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        Form form = new Form();
        form.setOwner(owner);
        form.setTitle(request.title());
        form.setDescription(request.description() == null ? "" : request.description());
        form.setSlug(generateSlug());

        return toDto(formRepository.save(form));
    }

    @Transactional(readOnly = true)
    public List<FormResponse> findAllForOwner(Long userId) {
        return formRepository.findByOwnerIdOrderByUpdatedAtDesc(userId)
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public FormResponse findById(Long userId, Long formId) {
        return toDto(requireOwnerForm(userId, formId));
    }

    @Transactional
    public FormResponse update(Long userId, Long formId, UpdateFormRequest request) {
        Form form = requireOwnerForm(userId, formId);

        form.setTitle(request.title());
        form.setDescription(request.description() == null ? "" : request.description());
        return toDto(form);
    }

    @Transactional
    public FormResponse publish(Long userId, Long formId) {
        Form form = requireOwnerForm(userId, formId);
        form.setStatus(FormStatus.PUBLISHED);
        return toDto(form);
    }

    @Transactional
    public FormResponse unpublish(Long userId, Long formId) {
        Form form = requireOwnerForm(userId, formId);
        form.setStatus(FormStatus.DRAFT);
        return toDto(form);
    }

    @Transactional(readOnly = true)
    public FormDetailsResponse getStructure(Long userId, Long formId) {
        Form form = requireOwnerForm(userId, formId);
        List<FormQuestion> questions = formQuestionRepository.findByFormIdOrderByPositionAsc(formId);
        return toDetailsDto(form, questions);
    }

    @Transactional
    public FormDetailsResponse updateStructure(Long userId, Long formId, UpdateFormStructureRequest request) {
        Form form = requireOwnerForm(userId, formId);

        if (formSubmissionRepository.existsByFormId(formId)) {
            throw new IllegalArgumentException("Cannot edit structure after receiving responses");
        }

        form.setTitle(request.title().trim());
        form.setDescription(safe(request.description()));
        form.setTheme(request.theme().trim());
        form.setAcceptingResponses(request.acceptingResponses());
        form.setPublicAccess(request.publicAccess());

        formQuestionRepository.deleteByFormId(formId);
        List<FormQuestion> questions = new ArrayList<>();
        List<FormQuestionRequest> incomingQuestions = request.questions() == null ? List.of() : request.questions();

        for (int i = 0; i < incomingQuestions.size(); i++) {
            FormQuestionRequest questionRequest = incomingQuestions.get(i);
            validateQuestion(questionRequest);

            FormQuestion question = new FormQuestion();
            question.setForm(form);
            question.setPosition(i);
            question.setSection(questionRequest.section() == null ? 0 : Math.max(0, questionRequest.section()));
            question.setTitle(questionRequest.title().trim());
            question.setDescription(safe(questionRequest.description()));
            question.setType(questionRequest.type());
            question.setRequired(questionRequest.required());
            question.setLinearMin(questionRequest.linearMin());
            question.setLinearMax(questionRequest.linearMax());
            question.setLinearMinLabel(trimToNull(questionRequest.linearMinLabel()));
            question.setLinearMaxLabel(trimToNull(questionRequest.linearMaxLabel()));

            List<FormQuestionOptionRequest> incomingOptions = questionRequest.options() == null
                ? List.of()
                : questionRequest.options();
            for (int optionIndex = 0; optionIndex < incomingOptions.size(); optionIndex++) {
                FormQuestionOptionRequest optionRequest = incomingOptions.get(optionIndex);
                FormQuestionOption option = new FormQuestionOption();
                option.setQuestion(question);
                option.setPosition(optionIndex);
                option.setValue(optionRequest.value().trim());
                option.setGoToSection(optionRequest.goToSection());
                question.getOptions().add(option);
            }

            questions.add(question);
        }

        formQuestionRepository.saveAll(questions);
        return toDetailsDto(form, questions);
    }

    @Transactional(readOnly = true)
    public PublicFormResponse getPublicForm(String slug) {
        Form form = requirePublicForm(slug);
        List<FormQuestion> questions = formQuestionRepository.findByFormIdOrderByPositionAsc(form.getId());
        return toPublicDto(form, questions);
    }

    @Transactional
    public SubmitFormResponse submitPublicForm(String slug, SubmitFormRequest request) {
        Form form = requirePublicForm(slug);
        if (!form.isAcceptingResponses()) {
            throw new IllegalArgumentException("This form is not accepting responses");
        }

        List<FormQuestion> questions = sortQuestions(formQuestionRepository.findByFormIdOrderByPositionAsc(form.getId()));
        Map<Long, FormQuestion> questionById = questions.stream()
            .collect(Collectors.toMap(FormQuestion::getId, q -> q));

        List<SubmitFormAnswerRequest> incomingAnswers = request.answers() == null ? List.of() : request.answers();
        Map<Long, SubmitFormAnswerRequest> answerByQuestionId = new HashMap<>();
        for (SubmitFormAnswerRequest answerRequest : incomingAnswers) {
            if (!questionById.containsKey(answerRequest.questionId())) {
                throw new IllegalArgumentException("Unknown question in submission");
            }
            if (answerByQuestionId.put(answerRequest.questionId(), answerRequest) != null) {
                throw new IllegalArgumentException("Duplicate answer for the same question");
            }
        }

        Set<Long> visitedQuestionIds = resolveVisitedQuestionIds(questions, answerByQuestionId);

        FormSubmission submission = new FormSubmission();
        submission.setForm(form);
        FormSubmission savedSubmission = formSubmissionRepository.save(submission);

        List<FormSubmissionAnswer> answersToSave = new ArrayList<>();
        for (FormQuestion question : questions) {
            if (!visitedQuestionIds.contains(question.getId())) {
                continue;
            }
            SubmitFormAnswerRequest answerRequest = answerByQuestionId.get(question.getId());
            String normalizedValue = normalizeAndValidate(question, answerRequest);
            if (normalizedValue == null) {
                continue;
            }

            FormSubmissionAnswer answer = new FormSubmissionAnswer();
            answer.setSubmission(savedSubmission);
            answer.setQuestion(question);
            answer.setValueText(normalizedValue);
            answersToSave.add(answer);
        }

        formSubmissionAnswerRepository.saveAll(answersToSave);
        return new SubmitFormResponse(savedSubmission.getId(), savedSubmission.getSubmittedAt());
    }

    @Transactional(readOnly = true)
    public List<FormSubmissionItemResponse> getSubmissions(Long userId, Long formId) {
        requireOwnerForm(userId, formId);
        return formSubmissionRepository.findByFormIdOrderBySubmittedAtDesc(formId)
            .stream()
            .map(submission -> new FormSubmissionItemResponse(submission.getId(), submission.getSubmittedAt()))
            .toList();
    }

    @Transactional(readOnly = true)
    public FormSubmissionDetailsResponse getSubmissionDetails(Long userId, Long formId, Long submissionId) {
        requireOwnerForm(userId, formId);
        FormSubmission submission = formSubmissionRepository.findById(submissionId)
            .orElseThrow(() -> new NotFoundException("Submission not found"));
        if (!submission.getForm().getId().equals(formId)) {
            throw new NotFoundException("Submission not found");
        }

        List<FormSubmissionAnswer> answers = formSubmissionAnswerRepository.findBySubmissionIdIn(List.of(submissionId));
        answers.sort(Comparator.comparingInt((FormSubmissionAnswer answer) -> answer.getQuestion().getSection())
            .thenComparingInt(answer -> answer.getQuestion().getPosition()));

        List<FormSubmissionAnswerResponse> responseAnswers = answers.stream()
            .map(answer -> new FormSubmissionAnswerResponse(
                answer.getQuestion().getId(),
                answer.getQuestion().getTitle(),
                answer.getQuestion().getType(),
                formatValueForDisplay(answer.getQuestion(), answer.getValueText())
            ))
            .toList();

        return new FormSubmissionDetailsResponse(submission.getId(), submission.getSubmittedAt(), responseAnswers);
    }

    @Transactional(readOnly = true)
    public FormResultsResponse getResults(Long userId, Long formId) {
        requireOwnerForm(userId, formId);
        List<FormQuestion> questions = sortQuestions(formQuestionRepository.findByFormIdOrderByPositionAsc(formId));
        List<FormSubmission> submissions = formSubmissionRepository.findByFormIdOrderBySubmittedAtDesc(formId);
        List<Long> submissionIds = submissions.stream().map(FormSubmission::getId).toList();

        List<FormSubmissionAnswer> answers = submissionIds.isEmpty()
            ? List.of()
            : formSubmissionAnswerRepository.findBySubmissionIdIn(submissionIds);
        Map<Long, List<FormSubmissionAnswer>> answersByQuestion = answers.stream()
            .collect(Collectors.groupingBy(answer -> answer.getQuestion().getId()));

        List<QuestionResultsResponse> results = questions.stream()
            .map(question -> toQuestionResults(question, answersByQuestion.getOrDefault(question.getId(), List.of())))
            .toList();

        return new FormResultsResponse(formId, submissions.size(), results);
    }

    @Transactional(readOnly = true)
    public String exportSubmissionsCsv(Long userId, Long formId) {
        requireOwnerForm(userId, formId);

        List<FormQuestion> questions = sortQuestions(formQuestionRepository.findByFormIdOrderByPositionAsc(formId));
        List<FormSubmission> submissions = formSubmissionRepository.findByFormIdOrderBySubmittedAtDesc(formId);
        Collections.reverse(submissions);

        List<Long> submissionIds = submissions.stream().map(FormSubmission::getId).toList();
        List<FormSubmissionAnswer> answers = submissionIds.isEmpty()
            ? List.of()
            : formSubmissionAnswerRepository.findBySubmissionIdIn(submissionIds);

        Map<Long, Map<Long, FormSubmissionAnswer>> answersBySubmission = new HashMap<>();
        for (FormSubmissionAnswer answer : answers) {
            answersBySubmission
                .computeIfAbsent(answer.getSubmission().getId(), ignored -> new HashMap<>())
                .put(answer.getQuestion().getId(), answer);
        }

        StringBuilder csv = new StringBuilder();
        List<String> headerColumns = new ArrayList<>();
        headerColumns.add("submission_id");
        headerColumns.add("submitted_at");
        for (FormQuestion question : questions) {
            headerColumns.add(question.getTitle());
        }
        csv.append(toCsvRow(headerColumns)).append('\n');

        for (FormSubmission submission : submissions) {
            List<String> row = new ArrayList<>();
            row.add(String.valueOf(submission.getId()));
            row.add(String.valueOf(submission.getSubmittedAt()));
            Map<Long, FormSubmissionAnswer> byQuestion = answersBySubmission.getOrDefault(submission.getId(), Map.of());
            for (FormQuestion question : questions) {
                FormSubmissionAnswer answer = byQuestion.get(question.getId());
                row.add(answer == null ? "" : formatValueForDisplay(question, answer.getValueText()));
            }
            csv.append(toCsvRow(row)).append('\n');
        }

        return csv.toString();
    }

    private String generateSlug() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private Form requireOwnerForm(Long userId, Long formId) {
        Form form = formRepository.findById(formId)
            .orElseThrow(() -> new NotFoundException("Form not found"));
        if (!form.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("You do not have access to this form");
        }
        return form;
    }

    private Form requirePublicForm(String slug) {
        Form form = formRepository.findBySlug(slug)
            .orElseThrow(() -> new NotFoundException("Form not found"));
        if (!form.isPublicAccess()) {
            throw new NotFoundException("Form not found");
        }
        if (form.getStatus() != FormStatus.PUBLISHED) {
            throw new IllegalArgumentException("Form is not published");
        }
        return form;
    }

    private void validateQuestion(FormQuestionRequest question) {
        if (question.type() == QuestionType.SINGLE_CHOICE
            || question.type() == QuestionType.MULTIPLE_CHOICE
            || question.type() == QuestionType.DROPDOWN) {
            List<FormQuestionOptionRequest> options = question.options() == null ? List.of() : question.options();
            if (options.size() < 2) {
                throw new IllegalArgumentException("Choice questions must have at least two options");
            }
        }

        if (question.type() == QuestionType.LINEAR_SCALE) {
            if (question.linearMin() == null || question.linearMax() == null) {
                throw new IllegalArgumentException("Linear scale requires min and max");
            }
            if (question.linearMin() >= question.linearMax()) {
                throw new IllegalArgumentException("Linear scale min must be less than max");
            }
        }

        if (question.section() != null && question.section() < 0) {
            throw new IllegalArgumentException("Section index must be >= 0");
        }

        List<FormQuestionOptionRequest> options = question.options() == null ? List.of() : question.options();
        for (FormQuestionOptionRequest option : options) {
            if (option.goToSection() != null && option.goToSection() < 0) {
                throw new IllegalArgumentException("Option go-to section must be >= 0");
            }
        }
    }

    private String normalizeAndValidate(FormQuestion question, SubmitFormAnswerRequest answerRequest) {
        if (answerRequest == null) {
            if (question.isRequired()) {
                throw new IllegalArgumentException("Required question is missing: " + question.getTitle());
            }
            return null;
        }

        return switch (question.getType()) {
            case SHORT_TEXT, PARAGRAPH -> normalizeTextAnswer(question, answerRequest.value());
            case SINGLE_CHOICE, DROPDOWN -> normalizeSingleChoiceAnswer(question, answerRequest.value());
            case MULTIPLE_CHOICE -> normalizeMultipleChoiceAnswer(question, answerRequest.values());
            case DATE -> normalizeDateAnswer(question, answerRequest.value());
            case TIME -> normalizeTimeAnswer(question, answerRequest.value());
            case LINEAR_SCALE -> normalizeScaleAnswer(question, answerRequest.value());
        };
    }

    private String normalizeTextAnswer(FormQuestion question, String rawValue) {
        String value = trimToNull(rawValue);
        if (value == null && question.isRequired()) {
            throw new IllegalArgumentException("Required question is missing: " + question.getTitle());
        }
        return value;
    }

    private String normalizeSingleChoiceAnswer(FormQuestion question, String rawValue) {
        String value = trimToNull(rawValue);
        if (value == null) {
            if (question.isRequired()) {
                throw new IllegalArgumentException("Required question is missing: " + question.getTitle());
            }
            return null;
        }

        Set<String> allowed = question.getOptions().stream()
            .map(FormQuestionOption::getValue)
            .collect(Collectors.toSet());
        if (!allowed.contains(value)) {
            throw new IllegalArgumentException("Invalid option for question: " + question.getTitle());
        }
        return value;
    }

    private String normalizeMultipleChoiceAnswer(FormQuestion question, List<String> rawValues) {
        List<String> values = (rawValues == null ? List.<String>of() : rawValues).stream()
            .map(this::trimToNull)
            .filter(v -> v != null)
            .distinct()
            .toList();

        if (values.isEmpty()) {
            if (question.isRequired()) {
                throw new IllegalArgumentException("Required question is missing: " + question.getTitle());
            }
            return null;
        }

        Set<String> allowed = question.getOptions().stream()
            .map(FormQuestionOption::getValue)
            .collect(Collectors.toSet());
        for (String value : values) {
            if (!allowed.contains(value)) {
                throw new IllegalArgumentException("Invalid option for question: " + question.getTitle());
            }
        }
        return toJson(values);
    }

    private String normalizeDateAnswer(FormQuestion question, String rawValue) {
        String value = trimToNull(rawValue);
        if (value == null) {
            if (question.isRequired()) {
                throw new IllegalArgumentException("Required question is missing: " + question.getTitle());
            }
            return null;
        }

        try {
            return LocalDate.parse(value).toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date for question: " + question.getTitle());
        }
    }

    private String normalizeTimeAnswer(FormQuestion question, String rawValue) {
        String value = trimToNull(rawValue);
        if (value == null) {
            if (question.isRequired()) {
                throw new IllegalArgumentException("Required question is missing: " + question.getTitle());
            }
            return null;
        }

        try {
            return LocalTime.parse(value).toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid time for question: " + question.getTitle());
        }
    }

    private String normalizeScaleAnswer(FormQuestion question, String rawValue) {
        String value = trimToNull(rawValue);
        if (value == null) {
            if (question.isRequired()) {
                throw new IllegalArgumentException("Required question is missing: " + question.getTitle());
            }
            return null;
        }

        try {
            int intValue = Integer.parseInt(value);
            if (question.getLinearMin() != null && intValue < question.getLinearMin()) {
                throw new IllegalArgumentException("Scale value is below minimum");
            }
            if (question.getLinearMax() != null && intValue > question.getLinearMax()) {
                throw new IllegalArgumentException("Scale value is above maximum");
            }
            return String.valueOf(intValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid scale value for question: " + question.getTitle());
        }
    }

    private QuestionResultsResponse toQuestionResults(FormQuestion question, List<FormSubmissionAnswer> answers) {
        List<QuestionBucketResponse> buckets;
        List<String> latestAnswers;

        if (question.getType() == QuestionType.SINGLE_CHOICE || question.getType() == QuestionType.DROPDOWN) {
            Map<String, Long> counts = new LinkedHashMap<>();
            for (FormQuestionOption option : question.getOptions()) {
                counts.put(option.getValue(), 0L);
            }
            for (FormSubmissionAnswer answer : answers) {
                counts.computeIfPresent(answer.getValueText(), (ignored, current) -> current + 1);
            }
            buckets = counts.entrySet().stream()
                .map(entry -> new QuestionBucketResponse(entry.getKey(), entry.getValue()))
                .toList();
            latestAnswers = List.of();
        } else if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
            Map<String, Long> counts = new LinkedHashMap<>();
            for (FormQuestionOption option : question.getOptions()) {
                counts.put(option.getValue(), 0L);
            }
            for (FormSubmissionAnswer answer : answers) {
                for (String value : fromJsonArray(answer.getValueText())) {
                    counts.computeIfPresent(value, (ignored, current) -> current + 1);
                }
            }
            buckets = counts.entrySet().stream()
                .map(entry -> new QuestionBucketResponse(entry.getKey(), entry.getValue()))
                .toList();
            latestAnswers = List.of();
        } else if (question.getType() == QuestionType.LINEAR_SCALE) {
            Map<String, Long> counts = new LinkedHashMap<>();
            if (question.getLinearMin() != null && question.getLinearMax() != null) {
                for (int i = question.getLinearMin(); i <= question.getLinearMax(); i++) {
                    counts.put(String.valueOf(i), 0L);
                }
            }
            for (FormSubmissionAnswer answer : answers) {
                counts.computeIfPresent(answer.getValueText(), (ignored, current) -> current + 1);
            }
            buckets = counts.entrySet().stream()
                .map(entry -> new QuestionBucketResponse(entry.getKey(), entry.getValue()))
                .toList();
            latestAnswers = List.of();
        } else {
            buckets = List.of();
            latestAnswers = answers.stream()
                .sorted(Comparator.comparing(answer -> answer.getSubmission().getSubmittedAt(), Comparator.reverseOrder()))
                .limit(10)
                .map(answer -> formatValueForDisplay(question, answer.getValueText()))
                .toList();
        }

        return new QuestionResultsResponse(question.getId(), question.getTitle(), question.getType(), buckets, latestAnswers);
    }

    private String formatValueForDisplay(FormQuestion question, String rawValue) {
        if (rawValue == null) {
            return "";
        }
        if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
            return String.join(" | ", fromJsonArray(rawValue));
        }
        return rawValue;
    }

    private List<String> fromJsonArray(String json) {
        try {
            return JSON.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private String toJson(List<String> values) {
        try {
            return JSON.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize answer");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String toCsvRow(List<String> values) {
        return values.stream()
            .map(this::csvEscape)
            .collect(Collectors.joining(","));
    }

    private String csvEscape(String value) {
        String safeValue = value == null ? "" : value;
        if (safeValue.contains(",") || safeValue.contains("\"") || safeValue.contains("\n")) {
            return "\"" + safeValue.replace("\"", "\"\"") + "\"";
        }
        return safeValue;
    }

    private FormDetailsResponse toDetailsDto(Form form, List<FormQuestion> questions) {
        return new FormDetailsResponse(
            form.getId(),
            form.getTitle(),
            form.getDescription(),
            form.getStatus(),
            form.getSlug(),
            form.getTheme(),
            form.isAcceptingResponses(),
            form.isPublicAccess(),
            form.getCreatedAt(),
            form.getUpdatedAt(),
            toQuestionDtos(questions)
        );
    }

    private PublicFormResponse toPublicDto(Form form, List<FormQuestion> questions) {
        return new PublicFormResponse(
            form.getId(),
            form.getTitle(),
            form.getDescription(),
            form.getSlug(),
            form.getStatus(),
            form.getTheme(),
            form.isAcceptingResponses(),
            toQuestionDtos(questions)
        );
    }

    private List<FormQuestionResponse> toQuestionDtos(List<FormQuestion> questions) {
        return questions.stream()
            .sorted(Comparator.comparingInt(FormQuestion::getSection).thenComparingInt(FormQuestion::getPosition))
            .map(question -> new FormQuestionResponse(
                question.getId(),
                question.getPosition(),
                question.getSection(),
                question.getTitle(),
                question.getDescription(),
                question.getType(),
                question.isRequired(),
                question.getOptions().stream()
                    .map(option -> new FormQuestionOptionResponse(
                        option.getId(),
                        option.getPosition(),
                        option.getValue(),
                        option.getGoToSection()
                    ))
                    .toList(),
                question.getLinearMin(),
                question.getLinearMax(),
                question.getLinearMinLabel(),
                question.getLinearMaxLabel()
            ))
            .toList();
    }

    private List<FormQuestion> sortQuestions(List<FormQuestion> questions) {
        return questions.stream()
            .sorted(Comparator.comparingInt(FormQuestion::getSection).thenComparingInt(FormQuestion::getPosition))
            .toList();
    }

    private Set<Long> resolveVisitedQuestionIds(
        List<FormQuestion> sortedQuestions,
        Map<Long, SubmitFormAnswerRequest> answerByQuestionId
    ) {
        Map<Integer, List<FormQuestion>> bySection = sortedQuestions.stream()
            .collect(Collectors.groupingBy(
                FormQuestion::getSection,
                LinkedHashMap::new,
                Collectors.toList()
            ));
        TreeSet<Integer> sectionOrder = new TreeSet<>(bySection.keySet());
        if (sectionOrder.isEmpty()) {
            return Set.of();
        }

        Set<Long> visitedQuestionIds = new LinkedHashSet<>();
        Set<Integer> visitedSections = new LinkedHashSet<>();
        Integer currentSection = sectionOrder.first();

        while (currentSection != null && !visitedSections.contains(currentSection)) {
            visitedSections.add(currentSection);
            List<FormQuestion> sectionQuestions = bySection.getOrDefault(currentSection, List.of());
            sectionQuestions.forEach(question -> visitedQuestionIds.add(question.getId()));

            Integer branchTarget = null;
            for (FormQuestion question : sectionQuestions) {
                if (question.getType() != QuestionType.SINGLE_CHOICE && question.getType() != QuestionType.DROPDOWN) {
                    continue;
                }
                SubmitFormAnswerRequest answerRequest = answerByQuestionId.get(question.getId());
                String selectedValue = answerRequest == null ? null : trimToNull(answerRequest.value());
                if (selectedValue == null) {
                    continue;
                }

                FormQuestionOption option = question.getOptions().stream()
                    .filter(candidate -> candidate.getValue().equals(selectedValue))
                    .findFirst()
                    .orElse(null);
                if (option != null && option.getGoToSection() != null && bySection.containsKey(option.getGoToSection())) {
                    branchTarget = option.getGoToSection();
                    break;
                }
            }

            if (branchTarget != null && !visitedSections.contains(branchTarget)) {
                currentSection = branchTarget;
                continue;
            }

            currentSection = sectionOrder.higher(currentSection);
        }

        return visitedQuestionIds;
    }

    private FormResponse toDto(Form form) {
        return new FormResponse(
            form.getId(),
            form.getTitle(),
            form.getDescription(),
            form.getStatus(),
            form.getSlug(),
            form.getTheme(),
            form.isAcceptingResponses(),
            form.isPublicAccess(),
            form.getCreatedAt(),
            form.getUpdatedAt()
        );
    }
}
