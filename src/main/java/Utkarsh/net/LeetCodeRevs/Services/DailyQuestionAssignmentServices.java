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
public class DailyQuestionAssignmentServices { // for daily ques assignment to each user by the weights n stuffs,

    @Autowired
    private UserRepository userRepository;

    //assignment to user based on questions only
    public void assignDailyQuestion(User user) {
        System.out.println("Assigning Ques by q type");
        Map<String, UserQuestionData> questions = user.getUserQuestions(); //getting user questions
        if (questions == null || questions.isEmpty()) {
            throw new RuntimeException("No questions available for assignment");
        }

        LocalDate today = LocalDate.now(); //time


        questions.values().forEach(q -> { //iterating through each question to check if last time it was assigned and if it's greater than a weak then cooldown is turned off(cooldown period is over) and it can be assigned again
            if (q.isCoolDown() && q.getLastAssigned() != null) {
                if (ChronoUnit.DAYS.between(q.getLastAssigned(), today) >= 7) {
                    q.setCoolDown(false);
                }
            }
        });

        // Filtering eligible questions by removing the questions still in cooldown period
        List<UserQuestionData> eligibleQuestions = questions.values().stream()
                .filter(q -> !q.isCoolDown())
                .toList();

        if (eligibleQuestions.isEmpty()) {
            throw new RuntimeException("No eligible questions to assign");
        }

        double totalWeight = eligibleQuestions.stream() //sum of teh total weights of eligible ques
                .mapToDouble(q -> 1.0/ q.getWeight())
                .sum();

        // Random weighted picking
        double rand = Math.random() * totalWeight; // picks random number between 0 and totalWeight
        double cumulative = 0;
        UserQuestionData selected = null; //just sucks, declaring it for storing the selected question
        for (UserQuestionData q : eligibleQuestions) { //iterating through each eligible questions
            cumulative += 1/q.getWeight(); // adds weight progressively
            if (rand <= cumulative) { // checks if random falls in this question's range
                selected = q; // picks this question
                break; //stops looking
            }
        }
        /* This code selects a question randomly but weighted so that questions with lower original weights
         have a higher chance of being selected. It does this by using the inverse of the weights (1/weight),
         summing those inverses, then picking a random value within that sum.
         It iterates through the questions accumulating their inverse weights until the cumulative sum exceeds
         the random value, selecting that question. This way, questions with smaller weights get proportionally
         larger chances to be picked.

         i.e
            Weighted Random Selection Using Inverse Weights:

            Given questions with original weights:
            Q1: 0.1, Q2: 0.2, Q3: 0.5, Q4: 2.0

            Calculate inverse weights (1/originalWeight):
            Q1: 10, Q2: 5, Q3: 2, Q4: 0.5

            Sum of inverse weights = 17.5

                        GRAPH
          ---------------------------------------
            |----Q1----|---Q2----|--Q3--|Q4|
            0         10       15     17    17.5
                          ↑
                        12 (random number)
          ---------------------------------------

            Assign intervals on a number line from 0 to total inverse weight:
            - Q1: 0 to 10
            - Q2: 10 to 15
            - Q3: 15 to 17
            - Q4: 17 to 17.5

            Pick a random number between 0 and 17.5 (e.g., 12)
            Select the question whose interval contains the random number:
            12 falls in Q2's interval (10 to 15) → select Q2

            This method ensures questions with smaller original weights
            have higher chances of selection.
            */



        user.setUserQuestions(questions);

        //assigning n updating the questions and daily ques
        if (selected != null) {
            selected.setCoolDown(true);
            selected.setLastAssigned(today);
//            selected.setWeight(Math.min(selected.getWeight() + 0.05, 2.0)); //update when solved, not added that method yet
            user.setDailyAssignedQuestionLink(selected.getLink());
        }
        System.out.println("Assigned Ques");
        userRepository.save(user);
    }

    //based on question tags
    public void assignTopicBasedQuestion(User user) {
        System.out.println("Assigning Ques by topics type");

        Map<String, UserQuestionData> questions = user.getUserQuestions(); //getting questions <title, ques>
        Map<String, Double> topicWeights = user.getTopicWeights(); //<topic, weight>
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

        //checking for ques cooldown as above method
        for (String topic : sortedTopics) {
            List<UserQuestionData> matching = questions.values().stream()
                    .filter(q -> !q.isCoolDown() &&
                            q.getTags() != null &&
                            q.getTags().contains(topic))
                    .toList();

            //weighting logic
            if (!matching.isEmpty()) {
                // weighted random pick
                double total = matching.stream().mapToDouble(q -> 1/q.getWeight()).sum(); // q is question
                double rand = Math.random() * total;
                double cumulative = 0;

                for (UserQuestionData q : matching) {
                    cumulative += 1/q.getWeight();
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
//        selected.setWeight(Math.min(2.0, selected.getWeight() + 0.05)); //no way to find if solved yet so no updating it
        user.setUserQuestions(questions);
        user.setTopicWeights(topicWeights);
        user.setDailyAssignedTopicQuestionLink(selected.getLink());
        System.out.println("Assigned Ques");

        userRepository.save(user);
    }

    @Async
    @Scheduled(cron = "00 00 04 * * ?") //at sec:min:hr(24)
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
    @Scheduled(cron = "00 00 04 * * ?") //at sec:min:hr(24)
    public void dailySchedulerForTagsQuestions() {
        System.out.println("Scheduler for Questions Triggered by Topics");
        List<User> userList = userRepository.findAll();
        for(User user : userList) { //for all users
            try {
                assignTopicBasedQuestion(user);
            } catch (Exception e) {
                System.err.println("Failed to assign Topic question for user: " + user.getEmail());
                e.printStackTrace();
            }
        }
    }
}
