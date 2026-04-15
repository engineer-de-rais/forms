package com.example.forms.form.repository;

import com.example.forms.form.entity.FormSubmissionAnswer;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormSubmissionAnswerRepository extends JpaRepository<FormSubmissionAnswer, Long> {
    List<FormSubmissionAnswer> findBySubmissionIdIn(Collection<Long> submissionIds);
}
