package Utkarsh.net.LeetCodeRevs.Services;

import Utkarsh.net.LeetCodeRevs.DTO.LeetCodeProblem;
import Utkarsh.net.LeetCodeRevs.Entity.DbQuestions;
import Utkarsh.net.LeetCodeRevs.Entity.TestCase;
import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Entity.UserQuestionData;
import Utkarsh.net.LeetCodeRevs.Repository.DbQuestionRepository;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Utkarsh.net.LeetCodeRevs.Controller.UserController.*;

@Service
public class DailyUpdateQuestionsAndWeightService {

    @Autowired
    private LeetCodeService leetCodeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DbQuestionServices dbQuestionServices;

    @Autowired
    private DbQuestionRepository dbQuestionRepository;

    //checks for the submissions of users and updates weight for the correct submissions and decrease for wrong ones
    public void updateWeightsForSubmissions(User user, Map<String, UserQuestionData> userQuestions) {
        List<Map<String, Object>> submissions = // List<<name, <timeStamp, status>>> //timestamp -> the question submission time, it is for checking if it's already processed or not; list for all 20 subs
                leetCodeService.getRecentSubmissionTitleTimestampStatus(user.getLeetCodeUserName()); //for getting the time psamps o the questions for checking if it's already checked and also for getting all recent submissions(last 20) for the user;
        Map<String, Double> topicWeights = user.getTopicWeights(); //getting topic weights to update
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
            if (!userQuestions.containsKey(title)) continue; //for new ques not in db, skipping for now

            UserQuestionData qData = userQuestions.get(title);

            String lastSeenTimestamp = qData.getLastUpdatedTimestamp(); //getting last updated ts
            if (lastSeenTimestamp != null && timeStamp.compareTo(lastSeenTimestamp) <= 0) { //we are skipping to next ques if timestamp is before or equal to the new ts(old question already checked or the same question)
                /*		compareTo() returns:
	                	0 if both timestamps are equal,
	                	< 0 if timeStamp is before lastSeenTimestamp,
	                	> 0 if timeStamp is after lastSeenTimestamp.*/
                continue;
            }
            double currentWeight = qData.getWeight();

            switch (status) {
                case "Accepted" -> { //i accepted then increasing the weight by 0.05
                    currentWeight = Math.min(2.0, currentWeight + 0.05);
                    qData.setWeight(currentWeight);
                }
                default -> { //if not then decreasing directly by 0.1, my gym think before submitting wrong ans 🤣
                    currentWeight = Math.max(0.1, currentWeight - 0.1);
                    qData.setWeight(currentWeight);
                }
            }
            qData.setLastUpdatedTimestamp(timeStamp); //setiing the new time stamp
            for (String tag : qData.getTags()) { //same for tags
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

    //added scheduling twice a day for each user to be updated twice daily with new questions solved
    public void refreshUserQuestionsAndWeights(User user) {
        Map<String, UserQuestionData> userQuestions = user.getUserQuestions();
        if (userQuestions == null) {
            userQuestions = new HashMap<>();
        }

        List<String> questionTitles;
        try {
            questionTitles = leetCodeService.getQuestionTitles(user.getLeetCodeUserName()); //getting questions from the api for last 20 correct submissions, returning it into a List of Strings in one go
        } catch (Exception e) {
            questionTitles = new ArrayList<>();
            System.err.println("Failed to fetch question titles from LeetCode API: " + e.getMessage());
        }
        for (String title : questionTitles) { // refer user controller for code comments, they are same,
            if (!userQuestions.containsKey(title)) {
                String link = leetCodeService.fetchLeetcodeLink(title);
                List<String> tags;
                List<TestCase> testCase = new ArrayList<>();
                boolean t = dbQuestionServices.findBy(title);
                String s = slugName(title);
                try {
                    if(t) {
                        DbQuestions dbQuestions = dbQuestionRepository.findByName(s);
                        tags = dbQuestions.getTags();
                        testCase = dbQuestions.getTestcases();
                    } else {
                        LeetCodeProblem leetCodeProblem = leetCodeService.fetchProblemData(link);
                        tags = leetCodeProblem.getTopicTags();
                        List<String> testCasesList = parseStringTestCases(leetCodeProblem.getExampleTestcases());
                        List<String> testCaseOutput = extractOutputsFromContent(leetCodeProblem.getContent());
                        for (int i = 0; i < Math.min(testCasesList.size(), testCaseOutput.size()); i++) {
                            testCase.add(new TestCase(testCasesList.get(i), testCaseOutput.get(i)));
                        }
                    }
                    UserQuestionData qData = new UserQuestionData();

                    qData.setTestCase(testCase);
                    qData.setTitle(title);
                    qData.setLink(link);
                    qData.setTags(tags);
                    qData.setWeight(1.0);
                    userQuestions.put(title, qData);
                } catch (Exception e) {
                    System.out.println("point1");

                    user.setUserQuestions(userQuestions);
                    userRepository.save(user);
                    System.out.println("Error With assigning : " + e);
                }
            }
        }

        System.out.println("point2");
        updateWeightsForSubmissions(user, userQuestions); //this one is for updating user questions and tags with new weights
        user.setUserQuestions(userQuestions); //saving the quess
        userRepository.save(user);
    }

    @CacheEvict(value = {"leetcodeTitles", "leetcodeLinks", "leetcodeTotalSubs"}, allEntries = true) //refreshing the cache for each api's daily after an interval of 12 hours and scheduling both upper methods
    @Scheduled(cron = "0 00 00,12 * * ?")
    public void scheduledRefresh() {
        System.out.println("Cache being reset");
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            System.out.println(user.getEmail());
            refreshUserQuestionsAndWeights(user); // Flow : at 0 and 12 daily -> scheduledRefresh() -> refreshUserQuestionsAndWeights(User) -> updateWeightsForSubmissions(user, userQuestionsFromDB)
        }
    }
}