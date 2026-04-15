package com.example.forms.form.repository;

import com.example.forms.form.entity.FormQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormQuestionRepository extends JpaRepository<FormQuestion, Long> {
    List<FormQuestion> findByFormIdOrderByPositionAsc(Long formId);

    void deleteByFormId(Long formId);
}
