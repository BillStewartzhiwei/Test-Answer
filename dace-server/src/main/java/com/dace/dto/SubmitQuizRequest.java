package com.dace.dto;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class SubmitQuizRequest {
    @Valid
    @NotEmpty
    private List<AnswerInput> answers;

    public List<AnswerInput> getAnswers() { return answers; }
    public void setAnswers(List<AnswerInput> answers) { this.answers = answers; }

    public static class AnswerInput {
        @NotNull
        private Long questionId;
        @NotNull
        private String selected;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public String getSelected() { return selected; }
        public void setSelected(String selected) { this.selected = selected; }
    }
}
