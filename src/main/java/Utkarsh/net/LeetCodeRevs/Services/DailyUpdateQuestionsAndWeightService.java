package Utkarsh.net.LeetCodeRevs.Services;

import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Entity.UserQuestionData;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DailyUpdateQuestionsAndWeightService {

    @Autowired
    private LeetCodeService leetCodeService;

    @Autowired
    private UserRepository userRepository;

    public void updateWeightsForSubmissions(User user, Map<String, UserQuestionData> userQuestions) {
        List<Map<String, Object>> submissions = leetCodeService.getRecentSubmissionTitleTimestampStatus(user.getLeetCodeUserName());
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
                    qData.setLastUpdatedTimestamp(timeStamp);
                }
                case "Accepted" -> {
                    currentWeight = Math.min(2.0, currentWeight + 0.05);
                    qData.setWeight(currentWeight);
                    qData.setLastUpdatedTimestamp(timeStamp);
                }
            }
        }
        user.setUserQuestions(userQuestions);
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
                try {
                    tags = leetCodeService.fetchProblemData(link).getTopicTags();
                } catch (Exception e) {
                    // handle or log error fetching tags
                }
                UserQuestionData qData = new UserQuestionData();
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
    @Scheduled(cron = "0 0 0,12 * * ?")
    public void scheduledRefresh() {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            refreshUserQuestionsAndWeights(user);
        }
    }
}