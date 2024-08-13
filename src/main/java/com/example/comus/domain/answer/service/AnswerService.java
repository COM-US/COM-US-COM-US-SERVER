package com.example.comus.domain.answer.service;

import com.example.comus.domain.answer.dto.request.AnswerRequestDto;
import com.example.comus.domain.answer.entity.Answer;
import com.example.comus.domain.question.repository.QuestionRepository;
import com.example.comus.domain.user.repository.UserRespository;
import com.example.comus.domain.user.entity.User;
import com.example.comus.domain.question.entity.Question;
import com.example.comus.domain.answer.repository.AnswerRepository;
import com.example.comus.domain.answer.dto.response.StatisticResponseDto;
import com.example.comus.domain.question.entity.Category;
import com.example.comus.domain.question.entity.AnswerType;
import com.example.comus.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.example.comus.global.error.ErrorCode.QUESTION_NOT_FOUND;
import static com.example.comus.global.error.ErrorCode.USER_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final UserRespository userRepository;
    private final QuestionRepository questionRepository;

    public StatisticResponseDto getAnswerStatistic(Long userId) {

        long totalAnswers = answerRepository.countByUserId(userId);
        if (totalAnswers == 0) {
            return new StatisticResponseDto(0, 0, 0, 0, 0, 0, 0, 0);
        }

        double sentenceRatio = getRatio("SENTENCE", totalAnswers, "answerType", userId);
        double multipleChoiceRatio = getRatio("MULTIPLE_CHOICE", totalAnswers, "answerType" , userId);
        double dailyQuestionRatio = getRatio("DAILY", totalAnswers, "category" , userId);
        double schoolQuestionRatio = getRatio("SCHOOL", totalAnswers, "category" , userId);
        double hobbyQuestionRatio = getRatio("FRIEND", totalAnswers, "category" , userId);
        double familyQuestionRatio = getRatio("FAMILY", totalAnswers, "category" , userId);
        double friendQuestionRatio = getRatio("HOBBY", totalAnswers, "category" , userId);
        double randomQuestionRatio = getRatio("RANDOM", totalAnswers, "category" , userId);

        return new StatisticResponseDto(
                sentenceRatio,
                multipleChoiceRatio,
                dailyQuestionRatio,
                schoolQuestionRatio,
                hobbyQuestionRatio,
                familyQuestionRatio,
                friendQuestionRatio,
                randomQuestionRatio
        );
    }

    private double getRatio(String input, long totalAnswerCnt, String type, Long userId) {
        long count = switch (type) {
            case "category" -> answerRepository.countByUserIdAndCategory(userId, Category.valueOf(input));
            case "answerType" -> answerRepository.countByUserIdAndAnswerType(userId, AnswerType.valueOf(input));
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };
        return calculatePercentage(count, totalAnswerCnt);
    }

    private double calculatePercentage(long count, long total) {
        double result = total == 0 ? 0 : (double) count / total * 100;
        return Math.round(result * 10) / 10.0;
    }


    public void createAnswer(Long userId, AnswerRequestDto answerRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        user.addChatTime();
        user.addChatCount();
        userRepository.save(user);

        Question question = questionRepository.findById(answerRequest.questionId())
                .orElseThrow(() -> new EntityNotFoundException(QUESTION_NOT_FOUND));

        Answer answer = Answer.builder()
                .user(user)
                .question(question)
                .answerContent(answerRequest.answerContent())
                .build();

        answerRepository.save(answer);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void resetTodayChatTime() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            user.resetTodayChatTime();
        }
        userRepository.saveAll(users);
    }

}
