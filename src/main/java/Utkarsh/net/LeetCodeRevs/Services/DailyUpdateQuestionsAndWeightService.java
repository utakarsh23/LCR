package Utkarsh.net.LeetCodeRevs.Services;

import Utkarsh.net.LeetCodeRevs.DTO.LeetCodeProblem;
import Utkarsh.net.LeetCodeRevs.Entity.TestCase;
import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Entity.UserQuestionData;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Utkarsh.net.LeetCodeRevs.Controller.UserController.extractOutputsFromContent;
import static Utkarsh.net.LeetCodeRevs.Controller.UserController.parseStringTestCases;

@Service
public class DailyUpdateQuestionsAndWeightService {

    @Autowired
    private LeetCodeService leetCodeService;

    @Autowired
    private UserRepository userRepository;

    public void updateWeightsForSubmissions(User user, Map<String, UserQuestionData> userQuestions) {
        List<Map<String, Object>> submissions = leetCodeService.getRecentSubmissionTitleTimestampStatus(user.getLeetCodeUserName());
        Map<String, Double> topicWeights = user.getTopicWeights();
//        List<Map<String, Object>> submissions = List.of(
//                Map.of("title", "Valid Parentheses", "statusDisplay", "Accepted", "timestamp", "1705842396"),
//                Map.of("title", "Combination Sum", "statusDisplay", "Wrong Answer", "timestamp", "1705842331"),
//                Map.of("title", "Remove Invalid Parentheses", "statusDisplay", "Time Limit Exceeded", "timestamp", "1705842200"),
//                Map.of("title", "Letter Combinations of a Phone Number", "statusDisplay", "Accepted", "timestamp", "1705842180"),
//                Map.of("title", "Ways to Express an Integer as Sum of Powers", "statusDisplay", "Wrong Answer", "timestamp", "1705842000"),
//                Map.of("title", "Combination Sum", "statusDisplay", "Wrong Answer", "timestamp", "1705842331"),
//                Map.of("title", "Generate Parentheses", "statusDisplay", "Wrong Answer", "timestamp", "1705841800"),
//                Map.of("title", "Combination Sum", "statusDisplay", "Wrong Answer", "timestamp", "1705842331"),
//                Map.of("title", "Minimum Number of Swaps to Make the String Balanced", "statusDisplay", "Wrong Answer", "timestamp", "1705841600"),
//                Map.of("title", "Sum of Two Integers", "statusDisplay", "Accepted", "timestamp", "1705841500"),
//
//                // unchanged bottom half
//                Map.of("title", "Perfect Squares", "statusDisplay", "Time Limit Exceeded", "timestamp", "1705841200"),
//                Map.of("title", "Count Primes", "statusDisplay", "Accepted", "timestamp", "1705841100"),
//                Map.of("title", "Ugly Number II", "statusDisplay", "Wrong Answer", "timestamp", "1705841000"),
//                Map.of("title", "Merge k Sorted Lists", "statusDisplay", "Accepted", "timestamp", "1705840900"),
//                Map.of("title", "Merge Two Sorted Lists", "statusDisplay", "Accepted", "timestamp", "1705840800"),
//                Map.of("title", "Sort List", "statusDisplay", "Time Limit Exceeded", "timestamp", "1705840700"),
//                Map.of("title", "Sort Colors", "statusDisplay", "Wrong Answer", "timestamp", "1705840600"),
//                Map.of("title", "Wiggle Sort II", "statusDisplay", "Accepted", "timestamp", "1705840500"),
//                Map.of("title", "Kth Largest Element in an Array", "statusDisplay", "Wrong Answer", "timestamp", "1705840400"),
//                Map.of("title", "K Closest Points to Origin", "statusDisplay", "Accepted", "timestamp", "1705840300")
//        );

        if (userQuestions == null || userQuestions.isEmpty()) {
            return;
        }
        if (topicWeights == null) {
            topicWeights = new HashMap<>();
        }

        System.out.println("huh");
        for (Map<String, Object> submission : submissions) {
            String title = (String) submission.get("title");
            String status = (String) submission.get("statusDisplay");
            String timeStamp = (String) submission.get("timestamp");
            if (!userQuestions.containsKey(title)) continue;

            UserQuestionData qData = userQuestions.get(title);

            String lastSeenTimestamp = qData.getLastUpdatedTimestamp();
            if (lastSeenTimestamp != null && timeStamp.compareTo(lastSeenTimestamp) <= 0) {
                continue;
            }
            double currentWeight = qData.getWeight();


            switch (status) {
                case "Wrong Answer", "Time Limit Exceeded" -> {
                    currentWeight = Math.max(0.1, currentWeight - 0.1);
                    qData.setWeight(currentWeight);
                }
                case "Accepted" -> {
                    currentWeight = Math.min(2.0, currentWeight + 0.05);
                    qData.setWeight(currentWeight);
                }
            }
            qData.setLastUpdatedTimestamp(timeStamp);
            for (String tag : qData.getTags()) {
                double topicWeight = topicWeights.getOrDefault(tag, 1.0);

                switch (status) {
                    case "Wrong Answer", "Time Limit Exceeded" ->
                            topicWeights.put(tag, Math.max(0.1, topicWeight - 0.05));
                    case "Accepted" ->
                            topicWeights.put(tag, Math.min(2.0, topicWeight + 0.02));
                }
            }
        }
        user.setTopicWeights(topicWeights);
        user.setUserQuestions(userQuestions);
        userRepository.save(user);
    }

    //for scheduling twice a day
    public void refreshUserQuestionsAndWeights(User user) {
        Map<String, UserQuestionData> userQuestions = user.getUserQuestions();
        if (userQuestions == null) {
            userQuestions = new HashMap<>();
        }

        List<String> questionTitles = leetCodeService.getQuestionTitles(user.getLeetCodeUserName());

        for (String title : questionTitles) {
            if (!userQuestions.containsKey(title)) {
                String link = leetCodeService.fetchLeetcodeLink(title);
                List<String> tags = new ArrayList<>();
                List<TestCase> testCases = List.of();
                try {
                    tags = leetCodeService.fetchProblemData(link).getTopicTags();
                    LeetCodeProblem leetCodeProblem = leetCodeService.fetchProblemData(link);
                    List<String> questionTags = leetCodeProblem.getTopicTags();
                    List<String> testCasesList = parseStringTestCases(leetCodeProblem.getExampleTestcases());
                    List<String> testCaseOutput = extractOutputsFromContent(leetCodeProblem.getContent());
                    for (int i = 0; i < Math.min(testCasesList.size(), testCaseOutput.size()); i++) {
                        testCases.add(new TestCase(testCasesList.get(i), testCaseOutput.get(i)));
                    }
                } catch (Exception e) {
                    System.out.println("Error With assigning : " + e);
                }
                UserQuestionData qData = new UserQuestionData();

                qData.setTestCase(testCases);
                qData.setTitle(title);
                qData.setLink(link);
                qData.setTags(tags);
                qData.setWeight(1.0);
                userQuestions.put(title, qData);
            }
        }

        updateWeightsForSubmissions(user, userQuestions);
        user.setUserQuestions(userQuestions);
        userRepository.save(user);
    }

    @CacheEvict(value = {"leetcodeTitles", "leetcodeLinks", "leetcodeTotalSubs"}, allEntries = true)
    @Scheduled(cron = "0 00 00,12 * * ?")
    public void scheduledRefresh() {
        System.out.println("Cache being reset");
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            refreshUserQuestionsAndWeights(user);
        }
    }
}