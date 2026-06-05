package com.dace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dace.common.BizException;
import com.dace.common.ErrorCode;
import com.dace.config.AuthContext;
import com.dace.config.CurrentUser;
import com.dace.dto.SubmitQuizRequest;
import com.dace.entity.Answer;
import com.dace.entity.Option;
import com.dace.entity.Question;
import com.dace.entity.Quiz;
import com.dace.entity.QuizAttempt;
import com.dace.mapper.AnswerMapper;
import com.dace.mapper.OptionMapper;
import com.dace.mapper.QuizAttemptMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttemptService {
    private final QuizAttemptMapper attemptMapper;
    private final AnswerMapper answerMapper;
    private final OptionMapper optionMapper;
    private final PermissionService permissionService;
    private final QuizService quizService;

    public AttemptService(QuizAttemptMapper attemptMapper, AnswerMapper answerMapper, OptionMapper optionMapper,
                          PermissionService permissionService, QuizService quizService) {
        this.attemptMapper = attemptMapper;
        this.answerMapper = answerMapper;
        this.optionMapper = optionMapper;
        this.permissionService = permissionService;
        this.quizService = quizService;
    }

    public Map<String, Object> start(Long quizId) {
        CurrentUser user = permissionService.requireRole("participant");
        Quiz quiz = quizService.requireQuiz(quizId);
        permissionService.requireMember(quiz.getSpaceId());
        if (!Integer.valueOf(2).equals(quiz.getStatus())) {
            throw new BizException(ErrorCode.QUIZ_NOT_AVAILABLE);
        }
        QuizAttempt existing = findAttempt(quizId, user.getId());
        if (existing != null) {
            if (Integer.valueOf(2).equals(existing.getStatus())) {
                throw new BizException(ErrorCode.ALREADY_SUBMITTED);
            }
            return attemptMap(existing, false);
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId(quizId);
        attempt.setUserId(user.getId());
        attempt.setSpaceId(quiz.getSpaceId());
        attempt.setScore(0);
        attempt.setTotalScore(quiz.getTotalScore());
        attempt.setStartAt(LocalDateTime.now());
        attempt.setStatus(1);
        attemptMapper.insert(attempt);
        return attemptMap(attempt, false);
    }

    @Transactional
    public Map<String, Object> submit(Long quizId, SubmitQuizRequest request) {
        CurrentUser user = permissionService.requireRole("participant");
        Quiz quiz = quizService.requireQuiz(quizId);
        permissionService.requireMember(quiz.getSpaceId());
        QuizAttempt attempt = findAttempt(quizId, user.getId());
        if (attempt == null) {
            start(quizId);
            attempt = findAttempt(quizId, user.getId());
        }
        if (Integer.valueOf(2).equals(attempt.getStatus())) {
            throw new BizException(ErrorCode.ALREADY_SUBMITTED);
        }

        Map<Long, String> submitted = request.getAnswers().stream()
            .collect(Collectors.toMap(SubmitQuizRequest.AnswerInput::getQuestionId, SubmitQuizRequest.AnswerInput::getSelected, (a, b) -> b));
        List<Question> questions = quizService.questions(quizId);
        int score = 0;
        int correctCount = 0;

        for (Question question : questions) {
            String selected = normalize(submitted.getOrDefault(question.getId(), ""));
            boolean correct = selected.equals(correctLabels(question.getId()));
            int earned = correct ? question.getScore() : 0;
            if (correct) {
                correctCount++;
            }
            score += earned;

            Answer answer = new Answer();
            answer.setAttemptId(attempt.getId());
            answer.setQuestionId(question.getId());
            answer.setSelectedOption(selected);
            answer.setIsCorrect(correct ? 1 : 0);
            answer.setScoreEarned(earned);
            answerMapper.insert(answer);
        }

        LocalDateTime submitAt = LocalDateTime.now();
        attempt.setScore(score);
        attempt.setTotalScore(quiz.getTotalScore());
        attempt.setSubmitAt(submitAt);
        attempt.setDurationSec((int) Duration.between(attempt.getStartAt(), submitAt).getSeconds());
        attempt.setStatus(2);
        attemptMapper.updateById(attempt);

        Map<String, Object> data = attemptMap(attempt, false);
        data.put("correct_count", correctCount);
        data.put("total_count", questions.size());
        data.put("rank", rankOf(attempt));
        return data;
    }

    public Map<String, Object> detail(Long attemptId) {
        QuizAttempt attempt = attemptMapper.selectById(attemptId);
        if (attempt == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        CurrentUser user = AuthContext.get();
        Quiz quiz = quizService.requireQuiz(attempt.getQuizId());
        if (!attempt.getUserId().equals(user.getId())) {
            permissionService.requireOwner(quiz.getSpaceId());
        }
        return attemptMap(attempt, true);
    }

    public int rankOf(QuizAttempt attempt) {
        Long better = attemptMapper.selectCount(new LambdaQueryWrapper<QuizAttempt>()
            .eq(QuizAttempt::getQuizId, attempt.getQuizId())
            .eq(QuizAttempt::getStatus, 2)
            .and(wrapper -> wrapper.gt(QuizAttempt::getScore, attempt.getScore())
                .or(inner -> inner.eq(QuizAttempt::getScore, attempt.getScore())
                    .lt(QuizAttempt::getDurationSec, attempt.getDurationSec()))));
        return better.intValue() + 1;
    }

    public QuizAttempt findAttempt(Long quizId, Long userId) {
        return attemptMapper.selectOne(new LambdaQueryWrapper<QuizAttempt>()
            .eq(QuizAttempt::getQuizId, quizId)
            .eq(QuizAttempt::getUserId, userId));
    }

    public Map<String, Object> attemptMap(QuizAttempt attempt, boolean includeAnswers) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("attempt_id", attempt.getId());
        data.put("id", attempt.getId());
        data.put("quiz_id", attempt.getQuizId());
        data.put("user_id", attempt.getUserId());
        data.put("space_id", attempt.getSpaceId());
        data.put("score", attempt.getScore());
        data.put("total_score", attempt.getTotalScore());
        data.put("start_at", attempt.getStartAt());
        data.put("submit_at", attempt.getSubmitAt());
        data.put("duration_sec", attempt.getDurationSec());
        data.put("status", attempt.getStatus());
        if (includeAnswers) {
            data.put("answers", answerMapper.selectList(new LambdaQueryWrapper<Answer>()
                    .eq(Answer::getAttemptId, attempt.getId()))
                .stream().map(this::answerMap).collect(Collectors.toList()));
        }
        return data;
    }

    private Map<String, Object> answerMap(Answer answer) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", answer.getId());
        data.put("attempt_id", answer.getAttemptId());
        data.put("question_id", answer.getQuestionId());
        data.put("selected_option", answer.getSelectedOption());
        data.put("is_correct", answer.getIsCorrect());
        data.put("score_earned", answer.getScoreEarned());
        return data;
    }

    private String correctLabels(Long questionId) {
        List<Option> correctOptions = optionMapper.selectList(new LambdaQueryWrapper<Option>()
            .eq(Option::getQuestionId, questionId)
            .eq(Option::getIsCorrect, 1)
            .orderByAsc(Option::getLabel));
        return correctOptions.stream().map(Option::getLabel).collect(Collectors.joining(","));
    }

    private String normalize(String selected) {
        Set<String> parts = Arrays.stream(selected.split(","))
            .map(String::trim)
            .filter(part -> !part.isEmpty())
            .collect(Collectors.toCollection(java.util.TreeSet::new));
        return String.join(",", parts);
    }
}
