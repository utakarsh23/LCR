package Utkarsh.net.LeetCodeRevs.Scheduler;

import Utkarsh.net.LeetCodeRevs.Entity.Questions;
import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import Utkarsh.net.LeetCodeRevs.Services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class QuestionScheduler {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "15 36 16 * * ?") // Runs daily at 4 PM UTC
    public void sendDailyQuestions() {
        Questions randomQuestion = questionService.getRandomQuestion();
        if (randomQuestion == null) {
            System.out.println("⚠️ No questions available in the database.");
            return;
        }

        // Fetch all users and assign the random daily question
        List<User> users = userRepository.findAll();
        for (User user : users) {
            user.setDailyQuestion(randomQuestion.getQuestionData().getTitle());
        }

        // Bulk save all users
        userRepository.saveAll(users);

        System.out.println("✅ Daily question assigned: " + randomQuestion.getQuestionData().getTitle());
    }
}