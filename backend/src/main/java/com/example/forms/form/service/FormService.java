package com.example.forms.form.service;

import com.example.forms.auth.entity.User;
import com.example.forms.auth.repository.UserRepository;
import com.example.forms.common.exception.NotFoundException;
import com.example.forms.form.dto.CreateFormRequest;
import com.example.forms.form.dto.FormResponse;
import com.example.forms.form.dto.UpdateFormRequest;
import com.example.forms.form.entity.Form;
import com.example.forms.form.entity.FormStatus;
import com.example.forms.form.repository.FormRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FormService {

    private final FormRepository formRepository;
    private final UserRepository userRepository;

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
    public FormResponse findById(Long formId) {
        Form form = formRepository.findById(formId)
            .orElseThrow(() -> new NotFoundException("Form not found"));
        return toDto(form);
    }

    @Transactional
    public FormResponse update(Long formId, UpdateFormRequest request) {
        Form form = formRepository.findById(formId)
            .orElseThrow(() -> new NotFoundException("Form not found"));

        form.setTitle(request.title());
        form.setDescription(request.description() == null ? "" : request.description());
        return toDto(form);
    }

    @Transactional
    public FormResponse publish(Long formId) {
        Form form = formRepository.findById(formId)
            .orElseThrow(() -> new NotFoundException("Form not found"));
        form.setStatus(FormStatus.PUBLISHED);
        return toDto(form);
    }

    @Transactional
    public FormResponse unpublish(Long formId) {
        Form form = formRepository.findById(formId)
            .orElseThrow(() -> new NotFoundException("Form not found"));
        form.setStatus(FormStatus.DRAFT);
        return toDto(form);
    }

    private String generateSlug() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private FormResponse toDto(Form form) {
        return new FormResponse(
            form.getId(),
            form.getTitle(),
            form.getDescription(),
            form.getStatus(),
            form.getSlug(),
            form.getCreatedAt(),
            form.getUpdatedAt()
        );
    }
}
