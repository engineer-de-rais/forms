package com.example.forms.form.dto;

import com.example.forms.form.entity.QuestionType;
import java.util.List;

public record QuestionResultsResponse(
    Long questionId,
    String title,
    QuestionType type,
    List<QuestionBucketResponse> buckets,
    List<String> latestAnswers
) {
}
