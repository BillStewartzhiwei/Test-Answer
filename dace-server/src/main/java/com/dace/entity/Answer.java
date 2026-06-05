package com.dace.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("answers")
public class Answer {
    private Long id;
    private Long attemptId;
    private Long questionId;
    private String selectedOption;
    private Integer isCorrect;
    private Integer scoreEarned;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAttemptId() { return attemptId; }
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getSelectedOption() { return selectedOption; }
    public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }
    public Integer getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Integer isCorrect) { this.isCorrect = isCorrect; }
    public Integer getScoreEarned() { return scoreEarned; }
    public void setScoreEarned(Integer scoreEarned) { this.scoreEarned = scoreEarned; }
}
