package com.dace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("quiz_attempts")
public class QuizAttempt {
    private Long id;
    private Long quizId;
    private Long userId;
    private Long spaceId;
    private Integer score;
    private Integer totalScore;
    private LocalDateTime startAt;
    private LocalDateTime submitAt;
    private Integer durationSec;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getSpaceId() { return spaceId; }
    public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getSubmitAt() { return submitAt; }
    public void setSubmitAt(LocalDateTime submitAt) { this.submitAt = submitAt; }
    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
