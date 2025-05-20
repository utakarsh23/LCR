package Utkarsh.net.LeetCodeRevs.Services;

import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Entity.UserQuestionData;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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

    //based on questions only
    public void assignDailyQuestion(User user) {
        System.out.println("Assigning Ques by q type");
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

        // Random weighted picking
        double rand = Math.random() * totalWeight;
        double cumulative = 0;
        UserQuestionData selected = null; //just sucks
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
            selected.setWeight(Math.min(selected.getWeight() + 0.05, 2.0));
            user.setDailyAssignedQuestionLink(selected.getLink());
        }
        System.out.println("Assigned Ques");
        userRepository.save(user);
    }

    //based on question tags
    public void assignTopicBasedQuestion(User user) {
        System.out.println("Assigning Ques by topics type");

        Map<String, UserQuestionData> questions = user.getUserQuestions();
        Map<String, Double> topicWeights = user.getTopicWeights();
        LocalDate today = LocalDate.now();

        if (topicWeights == null || topicWeights.isEmpty()) {
            throw new RuntimeException("No topic weights found for user");
        }

        // Sort topics by ascending weight (weakest first)
        List<String> sortedTopics = topicWeights.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        UserQuestionData selected = null;

        for (String topic : sortedTopics) {
            List<UserQuestionData> matching = questions.values().stream()
                    .filter(q -> !q.isCoolDown() &&
                            q.getTags() != null &&
                            q.getTags().contains(topic))
                    .toList();

            if (!matching.isEmpty()) {
                // weighted random pick
                double total = matching.stream().mapToDouble(UserQuestionData::getWeight).sum();
                double rand = Math.random() * total;
                double cumulative = 0;

                for (UserQuestionData q : matching) {
                    cumulative += q.getWeight();
                    if (rand <= cumulative) {
                        selected = q;
                        break;
                    }
                }
                if (selected != null) break; // found a question, stop looking further
            }
        }

        if (selected == null) {
            throw new RuntimeException("No eligible question found for any topic");
        }


        // Assign and update
        selected.setCoolDown(true);
        selected.setLastAssigned(today);
        selected.setWeight(Math.min(2.0, selected.getWeight() + 0.05));

        user.setUserQuestions(questions);
        user.setTopicWeights(topicWeights);
        user.setDailyAssignedTopicQuestionLink(selected.getLink());
        System.out.println("Assigned Ques");

        userRepository.save(user);
    }

    @Async
    @Scheduled(cron = "035 30 16 * * ?")
    public void dailyScheduler() {
        System.out.println("Scheduler for Questions by q Triggered");
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


    @Async
    @Scheduled(cron = "035 30 16 * * ?")
    public void dailySchedulerForTagsQuestions() {
        System.out.println("Scheduler for Questions Triggered by Topics");
        List<User> userList = userRepository.findAll();
        for(User user : userList) {
            try {
                assignTopicBasedQuestion(user);
            } catch (Exception e) {
                System.err.println("Failed to assign Topic question for user: " + user.getEmail());
                e.printStackTrace();
            }
        }
    }
}
