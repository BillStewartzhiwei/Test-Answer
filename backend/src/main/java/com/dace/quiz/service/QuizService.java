package com.dace.quiz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dace.quiz.common.BusinessException;
import com.dace.quiz.dto.ApiModels.OptionRequest;
import com.dace.quiz.dto.ApiModels.QuestionRequest;
import com.dace.quiz.dto.ApiModels.QuizRequest;
import com.dace.quiz.entity.OptionItem;
import com.dace.quiz.entity.Question;
import com.dace.quiz.entity.Quiz;
import com.dace.quiz.mapper.OptionItemMapper;
import com.dace.quiz.mapper.QuestionMapper;
import com.dace.quiz.mapper.QuizMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizMapper quizMapper;
    private final QuestionMapper questionMapper;
    private final OptionItemMapper optionMapper;
    private final GuardService guardService;

    @Transactional
    public Quiz create(Long userId, String role, QuizRequest request) {
        guardService.requireRole(role, "CREATOR");
        guardService.requireCreatorSpace(request.getSpaceId(), userId);
        Quiz quiz = new Quiz();
        quiz.setSpaceId(request.getSpaceId());
        quiz.setCreatorId(userId);
        quiz.setTitle(request.getTitle());
        quiz.setTimeLimitMinutes(request.getTimeLimitMinutes() == null ? 0 : request.getTimeLimitMinutes());
        quiz.setStatus("DRAFT");
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setUpdatedAt(LocalDateTime.now());
        quizMapper.insert(quiz);
        saveQuestions(quiz.getId(), request.getQuestions());
        refreshTotalScore(quiz);
        return quiz;
    }

    @Transactional
    public Quiz update(Long userId, Long quizId, QuizRequest request) {
        Quiz quiz = requireEditableQuiz(quizId, userId);
        quiz.setTitle(request.getTitle());
        quiz.setTimeLimitMinutes(request.getTimeLimitMinutes() == null ? 0 : request.getTimeLimitMinutes());
        quiz.setUpdatedAt(LocalDateTime.now());
        quizMapper.updateById(quiz);
        List<Question> oldQuestions = questionMapper.selectList(new LambdaQueryWrapper<Question>().eq(Question::getQuizId, quizId));
        for (Question question : oldQuestions) {
            optionMapper.delete(new LambdaQueryWrapper<OptionItem>().eq(OptionItem::getQuestionId, question.getId()));
        }
        questionMapper.delete(new LambdaQueryWrapper<Question>().eq(Question::getQuizId, quizId));
        saveQuestions(quizId, request.getQuestions());
        refreshTotalScore(quiz);
        return quizMapper.selectById(quizId);
    }

    public List<Quiz> listBySpace(Long userId, String role, Long spaceId) {
        if ("CREATOR".equals(role)) {
            guardService.requireCreatorSpace(spaceId, userId);
        } else {
            guardService.requireSpaceMember(spaceId, userId);
        }
        LambdaQueryWrapper<Quiz> query = new LambdaQueryWrapper<Quiz>()
                .eq(Quiz::getSpaceId, spaceId)
                .orderByDesc(Quiz::getCreatedAt);
        if ("PARTICIPANT".equals(role)) {
            query.eq(Quiz::getStatus, "PUBLISHED");
        }
        return quizMapper.selectList(query);
    }

    public Quiz detail(Long userId, String role, Long quizId) {
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null) {
            throw new BusinessException(404, "题目套不存在");
        }
        if ("CREATOR".equals(role)) {
            guardService.requireCreatorSpace(quiz.getSpaceId(), userId);
        } else {
            guardService.requireSpaceMember(quiz.getSpaceId(), userId);
            if (!"PUBLISHED".equals(quiz.getStatus())) {
                throw new BusinessException(403, "题目套未发布");
            }
        }
        return quiz;
    }

    public List<Question> questions(Long quizId) {
        return questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(Question::getQuizId, quizId)
                .orderByAsc(Question::getSortOrder));
    }

    public List<OptionItem> options(Long questionId) {
        return optionMapper.selectList(new LambdaQueryWrapper<OptionItem>()
                .eq(OptionItem::getQuestionId, questionId)
                .orderByAsc(OptionItem::getSortOrder));
    }

    public void publish(Long userId, Long quizId) {
        Quiz quiz = requireEditableQuiz(quizId, userId);
        if (questionMapper.selectCount(new LambdaQueryWrapper<Question>().eq(Question::getQuizId, quizId)) == 0) {
            throw new BusinessException("至少需要一道题");
        }
        quiz.setStatus("PUBLISHED");
        quiz.setPublishedAt(LocalDateTime.now());
        quiz.setUpdatedAt(LocalDateTime.now());
        quizMapper.updateById(quiz);
    }

    public void close(Long userId, Long quizId) {
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null) {
            throw new BusinessException(404, "题目套不存在");
        }
        guardService.requireCreatorSpace(quiz.getSpaceId(), userId);
        quiz.setStatus("CLOSED");
        quiz.setClosedAt(LocalDateTime.now());
        quiz.setUpdatedAt(LocalDateTime.now());
        quizMapper.updateById(quiz);
    }

    private Quiz requireEditableQuiz(Long quizId, Long userId) {
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null) {
            throw new BusinessException(404, "题目套不存在");
        }
        guardService.requireCreatorSpace(quiz.getSpaceId(), userId);
        if (!"DRAFT".equals(quiz.getStatus())) {
            throw new BusinessException("只有草稿可以编辑");
        }
        return quiz;
    }

    private void saveQuestions(Long quizId, List<QuestionRequest> requests) {
        if (requests == null) {
            return;
        }
        int order = 1;
        for (QuestionRequest request : requests) {
            validateQuestion(request);
            Question question = new Question();
            question.setQuizId(quizId);
            question.setType(request.getType());
            question.setTitle(request.getTitle());
            question.setScore(request.getScore() == null ? 1 : request.getScore());
            question.setSortOrder(request.getSortOrder() == null ? order : request.getSortOrder());
            question.setCreatedAt(LocalDateTime.now());
            question.setUpdatedAt(LocalDateTime.now());
            questionMapper.insert(question);
            int optionOrder = 1;
            for (OptionRequest optionRequest : request.getOptions()) {
                OptionItem option = new OptionItem();
                option.setQuestionId(question.getId());
                option.setContent(optionRequest.getContent());
                option.setCorrect(Boolean.TRUE.equals(optionRequest.getCorrect()));
                option.setSortOrder(optionRequest.getSortOrder() == null ? optionOrder : optionRequest.getSortOrder());
                option.setCreatedAt(LocalDateTime.now());
                option.setUpdatedAt(LocalDateTime.now());
                optionMapper.insert(option);
                optionOrder++;
            }
            order++;
        }
    }

    private void validateQuestion(QuestionRequest request) {
        if (!"SINGLE".equals(request.getType()) && !"MULTIPLE".equals(request.getType()) && !"JUDGE".equals(request.getType())) {
            throw new BusinessException("题型必须是 SINGLE/MULTIPLE/JUDGE");
        }
        if (request.getOptions() == null || request.getOptions().isEmpty()) {
            throw new BusinessException("题目必须包含选项");
        }
        long correctCount = request.getOptions().stream().filter(o -> Boolean.TRUE.equals(o.getCorrect())).count();
        if ("SINGLE".equals(request.getType()) && correctCount != 1) {
            throw new BusinessException("单选题只能有一个正确答案");
        }
        if ("JUDGE".equals(request.getType()) && request.getOptions().size() != 2) {
            throw new BusinessException("判断题必须有两个选项");
        }
        if (correctCount == 0) {
            throw new BusinessException("题目至少需要一个正确答案");
        }
    }

    private void refreshTotalScore(Quiz quiz) {
        List<Question> questions = questions(quiz.getId());
        int total = questions.stream().mapToInt(Question::getScore).sum();
        quiz.setTotalScore(total);
        quiz.setUpdatedAt(LocalDateTime.now());
        quizMapper.updateById(quiz);
    }
}
