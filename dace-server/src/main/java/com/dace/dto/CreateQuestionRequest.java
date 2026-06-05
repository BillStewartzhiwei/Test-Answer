package com.dace.dto;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class CreateQuestionRequest {
    @NotNull
    private Integer sortOrder;

    @NotBlank
    @Pattern(regexp = "single|multiple|judge")
    private String type;

    @NotBlank
    private String content;

    @NotNull
    private Integer score;

    @Valid
    @NotEmpty
    private List<OptionInput> options;

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public List<OptionInput> getOptions() { return options; }
    public void setOptions(List<OptionInput> options) { this.options = options; }

    public static class OptionInput {
        @NotBlank
        private String label;
        @NotBlank
        private String content;
        @NotNull
        private Boolean correct;

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Boolean getCorrect() { return correct; }
        public void setCorrect(Boolean correct) { this.correct = correct; }
    }
}
