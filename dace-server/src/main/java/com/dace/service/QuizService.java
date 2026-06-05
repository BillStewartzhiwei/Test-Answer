package com.dace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dace.common.BizException;
import com.dace.common.ErrorCode;
import com.dace.config.AuthContext;
import com.dace.dto.CreateQuestionRequest;
import com.dace.dto.CreateQuizRequest;
import com.dace.entity.Option;
import com.dace.entity.Question;
import com.dace.entity.Quiz;
import com.dace.entity.Space;
import com.dace.mapper.OptionMapper;
import com.dace.mapper.QuestionMapper;
import com.dace.mapper.QuizMapper;
import com.dace.mapper.SpaceMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizService {
    private final QuizMapper quizMapper;
    private final QuestionMapper questionMapper;
    private final OptionMapper optionMapper;
    private final SpaceMapper spaceMapper;
    private final PermissionService permissionService;

    public QuizService(QuizMapper quizMapper, QuestionMapper questionMapper, OptionMapper optionMapper,
                       SpaceMapper spaceMapper, PermissionService permissionService) {
        this.quizMapper = quizMapper;
        this.questionMapper = questionMapper;
        this.optionMapper = optionMapper;
        this.spaceMapper = spaceMapper;
        this.permissionService = permissionService;
    }

    public Map<String, Object> create(Long spaceId, CreateQuizRequest request) {
        permissionService.requireOwner(spaceId);
        Quiz quiz = new Quiz();
        quiz.setSpaceId(spaceId);
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTimeLimit(request.getTimeLimit());
        quiz.setTotalScore(0);
        quiz.setQuestionCount(0);
        quiz.setStatus(1);
        quiz.setCreatedAt(LocalDateTime.now());
        quizMapper.insert(quiz);

        Space space = spaceMapper.selectById(spaceId);
        spaceMapper.update(null, new LambdaUpdateWrapper<Space>()
            .eq(Space::getId, spaceId)
            .set(Space::getQuizCount, space.getQuizCount() + 1));
        return quizMap(quiz, false);
    }

    public List<Map<String, Object>> list(Long spaceId) {
        permissionService.requireMember(spaceId);
        return quizMapper.selectList(new LambdaQueryWrapper<Quiz>()
                .eq(Quiz::getSpaceId, spaceId)
                .orderByDesc(Quiz::getCreatedAt))
            .stream().map(quiz -> quizMap(quiz, false)).collect(Collectors.toList());
    }

    public Map<String, Object> detail(Long id) {
        Quiz quiz = requireQuiz(id);
        Space space = permissionService.requireMember(quiz.getSpaceId());
        boolean includeCorrect = space.getOwnerId().equals(AuthContext.get().getId());
        return quizMap(quiz, true, includeCorrect);
    }

    public Map<String, Object> update(Long id, CreateQuizRequest request) {
        Quiz quiz = requireQuiz(id);
        permissionService.requireOwner(quiz.getSpaceId());
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTimeLimit(request.getTimeLimit());
        quizMapper.updateById(quiz);
        return quizMap(quiz, false);
    }

    public Map<String, Object> publish(Long id) {
        Quiz quiz = requireQuiz(id);
        permissionService.requireOwner(quiz.getSpaceId());
        if (quiz.getQuestionCount() <= 0) {
            throw new BizException(ErrorCode.PARAM_ERROR);
        }
        quiz.setStatus(2);
        quiz.setPublishAt(LocalDateTime.now());
        quizMapper.updateById(quiz);
        return quizMap(quiz, false);
    }

    public Map<String, Object> close(Long id) {
        Quiz quiz = requireQuiz(id);
        permissionService.requireOwner(quiz.getSpaceId());
        quiz.setStatus(3);
        quizMapper.updateById(quiz);
        return quizMap(quiz, false);
    }

    @Transactional
    public Map<String, Object> addQuestion(Long quizId, CreateQuestionRequest request) {
        Quiz quiz = requireQuiz(quizId);
        permissionService.requireOwner(quiz.getSpaceId());
        validateQuestion(request);

        Question question = new Question();
        question.setQuizId(quizId);
        question.setSortOrder(request.getSortOrder());
        question.setType(request.getType());
        question.setContent(request.getContent());
        question.setScore(request.getScore());
        question.setCreatedAt(LocalDateTime.now());
        questionMapper.insert(question);

        for (CreateQuestionRequest.OptionInput input : request.getOptions()) {
            Option option = new Option();
            option.setQuestionId(question.getId());
            option.setLabel(input.getLabel());
            option.setContent(input.getContent());
            option.setIsCorrect(Boolean.TRUE.equals(input.getCorrect()) ? 1 : 0);
            optionMapper.insert(option);
        }
        recalcQuizStats(quizId);
        return questionMap(question, true);
    }

    @Transactional
    public Map<String, Object> updateQuestion(Long questionId, CreateQuestionRequest request) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        Quiz quiz = requireQuiz(question.getQuizId());
        permissionService.requireOwner(quiz.getSpaceId());
        validateQuestion(request);

        question.setSortOrder(request.getSortOrder());
        question.setType(request.getType());
        question.setContent(request.getContent());
        question.setScore(request.getScore());
        questionMapper.updateById(question);

        optionMapper.delete(new LambdaQueryWrapper<Option>().eq(Option::getQuestionId, questionId));
        for (CreateQuestionRequest.OptionInput input : request.getOptions()) {
            Option option = new Option();
            option.setQuestionId(questionId);
            option.setLabel(input.getLabel());
            option.setContent(input.getContent());
            option.setIsCorrect(Boolean.TRUE.equals(input.getCorrect()) ? 1 : 0);
            optionMapper.insert(option);
        }
        recalcQuizStats(quiz.getId());
        return questionMap(question, true);
    }

    @Transactional
    public Map<String, Object> deleteQuestion(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        Quiz quiz = requireQuiz(question.getQuizId());
        permissionService.requireOwner(quiz.getSpaceId());
        optionMapper.delete(new LambdaQueryWrapper<Option>().eq(Option::getQuestionId, questionId));
        questionMapper.deleteById(questionId);
        recalcQuizStats(quiz.getId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", questionId);
        data.put("deleted", true);
        return data;
    }

    public Quiz requireQuiz(Long id) {
        Quiz quiz = quizMapper.selectById(id);
        if (quiz == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        return quiz;
    }

    public List<Question> questions(Long quizId) {
        return questionMapper.selectList(new LambdaQueryWrapper<Question>()
            .eq(Question::getQuizId, quizId)
            .orderByAsc(Question::getSortOrder));
    }

    public List<Option> options(Long questionId) {
        return optionMapper.selectList(new LambdaQueryWrapper<Option>()
            .eq(Option::getQuestionId, questionId)
            .orderByAsc(Option::getLabel));
    }

    public Map<String, Object> quizMap(Quiz quiz, boolean includeQuestions) {
        return quizMap(quiz, includeQuestions, true);
    }

    public Map<String, Object> quizMap(Quiz quiz, boolean includeQuestions, boolean includeCorrect) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", quiz.getId());
        data.put("space_id", quiz.getSpaceId());
        data.put("title", quiz.getTitle());
        data.put("description", quiz.getDescription());
        data.put("time_limit", quiz.getTimeLimit());
        data.put("total_score", quiz.getTotalScore());
        data.put("question_count", quiz.getQuestionCount());
        data.put("status", quiz.getStatus());
        data.put("publish_at", quiz.getPublishAt());
        data.put("created_at", quiz.getCreatedAt());
        if (includeQuestions) {
            data.put("questions", questions(quiz.getId()).stream()
                .map(question -> questionMap(question, true, includeCorrect))
                .collect(Collectors.toList()));
        }
        return data;
    }

    public Map<String, Object> questionMap(Question question, boolean includeOptions) {
        return questionMap(question, includeOptions, true);
    }

    public Map<String, Object> questionMap(Question question, boolean includeOptions, boolean includeCorrect) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", question.getId());
        data.put("quiz_id", question.getQuizId());
        data.put("sort_order", question.getSortOrder());
        data.put("type", question.getType());
        data.put("content", question.getContent());
        data.put("score", question.getScore());
        if (includeOptions) {
            data.put("options", options(question.getId()).stream()
                .map(option -> optionMap(option, includeCorrect))
                .collect(Collectors.toList()));
        }
        return data;
    }

    private Map<String, Object> optionMap(Option option) {
        return optionMap(option, true);
    }

    private Map<String, Object> optionMap(Option option, boolean includeCorrect) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", option.getId());
        data.put("question_id", option.getQuestionId());
        data.put("label", option.getLabel());
        data.put("content", option.getContent());
        if (includeCorrect) {
            data.put("is_correct", option.getIsCorrect());
        }
        return data;
    }

    private void recalcQuizStats(Long quizId) {
        List<Question> questions = questions(quizId);
        int totalScore = questions.stream().mapToInt(Question::getScore).sum();
        quizMapper.update(null, new LambdaUpdateWrapper<Quiz>()
            .eq(Quiz::getId, quizId)
            .set(Quiz::getQuestionCount, questions.size())
            .set(Quiz::getTotalScore, totalScore));
    }

    private void validateQuestion(CreateQuestionRequest request) {
        long correctCount = request.getOptions().stream().filter(option -> Boolean.TRUE.equals(option.getCorrect())).count();
        if ("single".equals(request.getType()) && correctCount != 1) {
            throw new BizException(ErrorCode.PARAM_ERROR);
        }
        if ("judge".equals(request.getType()) && (request.getOptions().size() != 2 || correctCount != 1)) {
            throw new BizException(ErrorCode.PARAM_ERROR);
        }
        if ("multiple".equals(request.getType()) && correctCount < 1) {
            throw new BizException(ErrorCode.PARAM_ERROR);
        }
    }
}
