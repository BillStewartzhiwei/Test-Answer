package com.dace.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("options")
public class Option {
    private Long id;
    private Long questionId;
    private String label;
    private String content;
    private Integer isCorrect;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Integer isCorrect) { this.isCorrect = isCorrect; }
}
