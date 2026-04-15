package com.example.forms.form.repository;

import com.example.forms.form.entity.FormSubmission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormSubmissionRepository extends JpaRepository<FormSubmission, Long> {
    boolean existsByFormId(Long formId);

    List<FormSubmission> findByFormIdOrderBySubmittedAtDesc(Long formId);
}
