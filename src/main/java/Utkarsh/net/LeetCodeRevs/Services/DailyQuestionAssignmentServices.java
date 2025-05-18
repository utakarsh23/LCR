package Utkarsh.net.LeetCodeRevs.Services;

import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Entity.UserQuestionData;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DailyQuestionAssignmentServices {

    @Autowired
    private UserRepository userRepository;


    public void assignDailyQuestion(User user) {
        Map<String, UserQuestionData> questions = user.getUserQuestions();
        if (questions == null || questions.isEmpty()) {
            throw new RuntimeException("No questions available for assignment");
        }

        LocalDate today = LocalDate.now();


        questions.values().forEach(q -> {
            if (q.isCoolDown() && q.getLastAssigned() != null) {
                if (ChronoUnit.DAYS.between(q.getLastAssigned(), today) >= 7) {
                    q.setCoolDown(false);
                }
            }
        });

        // Filtering eligible questions
        List<UserQuestionData> eligibleQuestions = questions.values().stream()
                .filter(q -> !q.isCoolDown())
                .toList();

        if (eligibleQuestions.isEmpty()) {
            throw new RuntimeException("No eligible questions to assign");
        }

        double totalWeight = eligibleQuestions.stream()
                .mapToDouble(UserQuestionData::getWeight)
                .sum();

        // Random weighted pick
        double rand = Math.random() * totalWeight;
        double cumulative = 0;
        UserQuestionData selected = null;
        for (UserQuestionData q : eligibleQuestions) {
            cumulative += q.getWeight();
            if (rand <= cumulative) {
                selected = q;
                break;
            }
        }


        user.setUserQuestions(questions);

        //assigning n updating the questions and daily ques
        if (selected != null) {
            selected.setCoolDown(true);
            selected.setLastAssigned(today);
            selected.setWeight(Math.min(selected.getWeight() + 0.1, 2.0));

            user.setDailyAssignedQuestionLink(selected.getLink());
        }
        userRepository.save(user);
    }
    @Scheduled(cron = "0 0 4 * * ?")
    public void dailyScheduler() {
        List<User> userList = userRepository.findAll();
        for(User user : userList) {
            try {
                assignDailyQuestion(user);
            } catch (Exception e) {
                System.err.println("Failed to assign question for user: " + user.getEmail());
                e.printStackTrace();
            }
        }
    }
}
