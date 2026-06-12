package com.dace.quiz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dace.quiz.common.BusinessException;
import com.dace.quiz.dto.ApiModels.AnswerDetail;
import com.dace.quiz.dto.ApiModels.AnswerRequest;
import com.dace.quiz.dto.ApiModels.AttemptResult;
import com.dace.quiz.dto.ApiModels.SubmitAttemptRequest;
import com.dace.quiz.entity.Answer;
import com.dace.quiz.entity.OptionItem;
import com.dace.quiz.entity.Question;
import com.dace.quiz.entity.Quiz;
import com.dace.quiz.entity.QuizAttempt;
import com.dace.quiz.mapper.AnswerMapper;
import com.dace.quiz.mapper.OptionItemMapper;
import com.dace.quiz.mapper.QuestionMapper;
import com.dace.quiz.mapper.QuizAttemptMapper;
import com.dace.quiz.mapper.QuizMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttemptService {
    private final QuizMapper quizMapper;
    private final QuestionMapper questionMapper;
    private final OptionItemMapper optionMapper;
    private final QuizAttemptMapper attemptMapper;
    private final AnswerMapper answerMapper;
    private final GuardService guardService;

    @Transactional
    public AttemptResult submit(Long userId, String role, Long quizId, SubmitAttemptRequest request) {
        guardService.requireRole(role, "PARTICIPANT");
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null || !"PUBLISHED".equals(quiz.getStatus())) {
            throw new BusinessException("只能作答已发布题目");
        }
        guardService.requireSpaceMember(quiz.getSpaceId(), userId);
        if (attemptMapper.selectCount(new LambdaQueryWrapper<QuizAttempt>()
                .eq(QuizAttempt::getQuizId, quizId)
                .eq(QuizAttempt::getUserId, userId)) > 0) {
            throw new BusinessException("每套题只能作答一次");
        }
        int duration = request.getDurationSeconds() == null ? 0 : request.getDurationSeconds();
        if (quiz.getTimeLimitMinutes() != null && quiz.getTimeLimitMinutes() > 0 && duration > quiz.getTimeLimitMinutes() * 60) {
            throw new BusinessException("已超过答题时限");
        }

        List<Question> questions = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(Question::getQuizId, quizId)
                .orderByAsc(Question::getSortOrder));
        Map<Long, AnswerRequest> submitted = request.getAnswers() == null
                ? java.util.Collections.emptyMap()
                : request.getAnswers().stream().collect(Collectors.toMap(AnswerRequest::getQuestionId, a -> a, (a, b) -> b));

        int score = 0;
        int correctCount = 0;
        List<AnswerDetail> details = new ArrayList<>();
        List<Answer> answers = new ArrayList<>();

        for (Question question : questions) {
            List<OptionItem> options = optionMapper.selectList(new LambdaQueryWrapper<OptionItem>()
                    .eq(OptionItem::getQuestionId, question.getId())
                    .orderByAsc(OptionItem::getSortOrder));
            Set<Long> correctIds = options.stream()
                    .filter(OptionItem::getCorrect)
                    .map(OptionItem::getId)
                    .collect(Collectors.toCollection(HashSet::new));
            Set<Long> selectedIds = new HashSet<>();
            AnswerRequest answerRequest = submitted.get(question.getId());
            if (answerRequest != null && answerRequest.getSelectedOptionIds() != null) {
                selectedIds.addAll(answerRequest.getSelectedOptionIds());
            }
            boolean correct = selectedIds.equals(correctIds);
            int questionScore = correct ? question.getScore() : 0;
            if (correct) {
                score += question.getScore();
                correctCount++;
            }

            Answer answer = new Answer();
            answer.setQuestionId(question.getId());
            answer.setSelectedOptionIds(selectedIds.stream().sorted().map(String::valueOf).collect(Collectors.joining(",")));
            answer.setCorrect(correct);
            answer.setScore(questionScore);
            answer.setCreatedAt(LocalDateTime.now());
            answers.add(answer);

            AnswerDetail detail = new AnswerDetail();
            detail.setQuestionId(question.getId());
            detail.setTitle(question.getTitle());
            detail.setScore(questionScore);
            detail.setCorrect(correct);
            detail.setSelectedOptionIds(selectedIds.stream().sorted().collect(Collectors.toList()));
            detail.setCorrectOptionIds(correctIds.stream().sorted().collect(Collectors.toList()));
            details.add(detail);
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId(quizId);
        attempt.setSpaceId(quiz.getSpaceId());
        attempt.setUserId(userId);
        attempt.setScore(score);
        attempt.setTotalScore(quiz.getTotalScore());
        attempt.setCorrectCount(correctCount);
        attempt.setQuestionCount(questions.size());
        attempt.setDurationSeconds(duration);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setCreatedAt(LocalDateTime.now());
        attemptMapper.insert(attempt);

        for (Answer answer : answers) {
            answer.setAttemptId(attempt.getId());
            answerMapper.insert(answer);
        }

        AttemptResult result = buildResult(attempt, details);
        result.setRank(resolveQuizRank(quizId, userId));
        return result;
    }

    public AttemptResult result(Long userId, Long attemptId) {
        QuizAttempt attempt = attemptMapper.selectById(attemptId);
        if (attempt == null || !userId.equals(attempt.getUserId())) {
            throw new BusinessException(404, "作答记录不存在");
        }
        List<Answer> answers = answerMapper.selectList(new LambdaQueryWrapper<Answer>().eq(Answer::getAttemptId, attemptId));
        List<AnswerDetail> details = answers.stream().map(answer -> {
            Question question = questionMapper.selectById(answer.getQuestionId());
            List<Long> selected = splitIds(answer.getSelectedOptionIds());
            List<Long> correct = optionMapper.selectList(new LambdaQueryWrapper<OptionItem>()
                    .eq(OptionItem::getQuestionId, answer.getQuestionId())
                    .eq(OptionItem::getCorrect, true))
                    .stream().map(OptionItem::getId).sorted().collect(Collectors.toList());
            AnswerDetail detail = new AnswerDetail();
            detail.setQuestionId(answer.getQuestionId());
            detail.setTitle(question == null ? "" : question.getTitle());
            detail.setScore(answer.getScore());
            detail.setCorrect(answer.getCorrect());
            detail.setSelectedOptionIds(selected);
            detail.setCorrectOptionIds(correct);
            return detail;
        }).collect(Collectors.toList());
        AttemptResult result = buildResult(attempt, details);
        result.setRank(resolveQuizRank(attempt.getQuizId(), userId));
        return result;
    }

    private AttemptResult buildResult(QuizAttempt attempt, List<AnswerDetail> details) {
        AttemptResult result = new AttemptResult();
        result.setAttemptId(attempt.getId());
        result.setScore(attempt.getScore());
        result.setTotalScore(attempt.getTotalScore());
        result.setCorrectCount(attempt.getCorrectCount());
        result.setQuestionCount(attempt.getQuestionCount());
        result.setDurationSeconds(attempt.getDurationSeconds());
        result.setAccuracy(attempt.getQuestionCount() == 0 ? 0 : attempt.getCorrectCount() * 1.0 / attempt.getQuestionCount());
        result.setDetails(details);
        return result;
    }

    private Integer resolveQuizRank(Long quizId, Long userId) {
        List<QuizAttempt> attempts = attemptMapper.selectList(new LambdaQueryWrapper<QuizAttempt>().eq(QuizAttempt::getQuizId, quizId));
        attempts.sort(Comparator.comparing(QuizAttempt::getScore).reversed()
                .thenComparing(QuizAttempt::getDurationSeconds)
                .thenComparing(QuizAttempt::getSubmittedAt));
        for (int i = 0; i < attempts.size(); i++) {
            if (userId.equals(attempts.get(i).getUserId())) {
                return i + 1;
            }
        }
        return null;
    }

    private List<Long> splitIds(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        String[] parts = value.split(",");
        List<Long> ids = new ArrayList<>();
        for (String part : parts) {
            ids.add(Long.valueOf(part));
        }
        return ids;
    }
}
