package Utkarsh.net.LeetCodeRevs.Controller;

import Utkarsh.net.LeetCodeRevs.DTO.LeetCodeProblem;
import Utkarsh.net.LeetCodeRevs.Entity.DbQuestions;
import Utkarsh.net.LeetCodeRevs.Entity.TestCase;
import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Entity.UserQuestionData;
import Utkarsh.net.LeetCodeRevs.Repository.DbQuestionRepository;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import Utkarsh.net.LeetCodeRevs.Services.DailyUpdateQuestionsAndWeightService;
import Utkarsh.net.LeetCodeRevs.Services.DbQuestionServices;
import Utkarsh.net.LeetCodeRevs.Services.LeetCodeService;
import Utkarsh.net.LeetCodeRevs.Services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeetCodeService leetCodeService;

    @Autowired
    private DbQuestionRepository dbQuestionRepository;

    @Autowired
    private DbQuestionServices dbQuestionServices;

    private static final ObjectMapper mapper = new ObjectMapper();


    @Autowired
    private DailyUpdateQuestionsAndWeightService dailyUpdateQuestionsAndWeightService;

    @GetMapping("/cli/daily-question")
    public ResponseEntity<Map<Object, Map<String, Object>>> getDailyQuestion() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Map<String, Object> inner = new HashMap<>();
        Map<Object, Map<String, Object>> outer = new HashMap<>();

        System.out.println("lmao");
        System.out.println("lmao1");

        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            return buildErrorResponse("User not found");
        }

        System.out.println("lmao2");

        // Add links
        inner.put("dailyQuestionLink", user.getDailyAssignedQuestionLink());
        inner.put("topicQuestionLink", user.getDailyAssignedTopicQuestionLink());

        // Add test cases if available
        String dqLink = user.getDailyAssignedQuestionLink();
        String tqLink = user.getDailyAssignedTopicQuestionLink();

        if (dqLink != null) {
            UserQuestionData dqData = user.getUserQuestions().values().stream()
                    .filter(q -> dqLink.equals(q.getLink()))
                    .findFirst()
                    .orElse(null);
            inner.put("dailyQuestionTestCases", dqData != null ? dqData.getTestCase() : null);
        } else {
            inner.put("dailyQuestionTestCases", null);
        }

        if (tqLink != null) {
            UserQuestionData tqData = user.getUserQuestions().values().stream()
                    .filter(q -> tqLink.equals(q.getLink()))
                    .findFirst()
                    .orElse(null);
            inner.put("topicQuestionTestCases", tqData != null ? tqData.getTestCase() : null);
        } else {
            inner.put("topicQuestionTestCases", null);
        }

        outer.put("data", inner);
        return ResponseEntity.ok(outer);
    }

    private ResponseEntity<Map<Object, Map<String, Object>>> buildErrorResponse(String errorMsg) {
        Map<String, Object> innerResponse = new HashMap<>();
        innerResponse.put("error", errorMsg);
        innerResponse.put("dailyQuestionLink", null);
        innerResponse.put("topicQuestionLink", null);

        Map<Object, Map<String, Object>> outerResponse = new HashMap<>();
        outerResponse.put("response", innerResponse);

        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(outerResponse);
    }


    //prolly the worst code ever
    @GetMapping("getProfile")
    public ResponseEntity<Object> findUserByEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        System.out.println("yayyy");
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Map<String, UserQuestionData> userQuestions = user.getUserQuestions();
        if (userQuestions == null) {
            userQuestions = new HashMap<>();
        }

        List<String> questionTitles;
        try {
            questionTitles = leetCodeService.getQuestionTitles(user.getLeetCodeUserName());
//            questionTitles = List.of(
//                    "Valid Parentheses",
//                    "Combination Sum",
//                    "Remove Invalid Parentheses",
//                    "Letter Combinations of a Phone Number",
//                    "Ways to Express an Integer as Sum of Powers",
//                    "Sign of the Product of an Array",
//                    "Generate Parentheses",
//                    "Combination Sum IV",
//                    "Minimum Number of Swaps to Make the String Balanced",
//                    "Sum of Two Integers",
//                    "Perfect Squares",
//                    "Count Primes",
//                    "Ugly Number II",
//                    "Merge k Sorted Lists",
//                    "Merge Two Sorted Lists",
//                    "Sort List",
//                    "Sort Colors",
//                    "Wiggle Sort II",
//                    "Kth Largest Element in an Array",
//                    "K Closest Points to Origin"
//            );
        } catch (Exception e) {
            return ResponseEntity.ok("Failed to fetch question titles from LeetCode API: " + e.getMessage());
        }


        for (String title : questionTitles) {
            if (!userQuestions.containsKey(title)) {
                try {
                    String linkOfQuestion = leetCodeService.fetchLeetcodeLink(title);
                    List<String> questionTags;
                    List<TestCase> testCase = new ArrayList<>();
                    boolean t = dbQuestionServices.findBy(title);
                    String s = slugName(title);
                    if(t) {
                        DbQuestions dbQuestions = dbQuestionRepository.findByName(s);
                        questionTags = dbQuestions.getTags();
                        testCase = dbQuestions.getTestcases();
                    } else {
                        LeetCodeProblem leetCodeProblem = leetCodeService.fetchProblemData(linkOfQuestion);
                        questionTags = leetCodeProblem.getTopicTags();
                        List<String> testCasesList = parseStringTestCases(leetCodeProblem.getExampleTestcases());
                        List<String> testCaseOutput = extractOutputsFromContent(leetCodeProblem.getContent());
                        for (int i = 0; i < Math.min(testCasesList.size(), testCaseOutput.size()); i++) {
                            testCase.add(new TestCase(testCasesList.get(i), testCaseOutput.get(i)));
                        }
                    }

                    UserQuestionData questionData = new UserQuestionData();
                    questionData.setTitle(title);
                    questionData.setLink(linkOfQuestion);
                    questionData.setTags(questionTags);
                    questionData.setTestCase(testCase);
                    questionData.setWeight(1.0);

                    userQuestions.put(title, questionData);
                } catch (Exception e) {
                    //in case api failes, it still saves the ftetches ques
                    user.setUserQuestions(userQuestions);
                    userRepository.save(user);
                    return ResponseEntity.badRequest().body("Failed to fetch link or tags for title: " + title + " - " + e.getMessage());
                }
            }
        }

        dailyUpdateQuestionsAndWeightService.updateWeightsForSubmissions(user, userQuestions);

        user.setUserQuestions(userQuestions);
        userRepository.save(user);

        System.out.println(user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/deleteUser")
    private ResponseEntity<Boolean> deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findUserByEmail(email);
        if (email != null && !email.isEmpty()) {
            userRepository.deleteById(user.getId());
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
    }

    @PutMapping("/updateUser")
    private ResponseEntity<?> updateUser(@RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User userInDb = userRepository.findUserByEmail(email);
            userInDb.setEmail(user.getEmail());
            userInDb.setPassword(user.getPassword());
            userInDb.setLeetCodeUserName(user.getLeetCodeUserName());
            userService.createUser(userInDb);
            return new ResponseEntity<>(user,HttpStatus.FOUND);
    }

    @PostMapping("/signout")
    public void signOut() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        //wait
    }

    public static List<String> parseStringTestCases(String rawTestCases) {
//        List<String> result = new ArrayList<>();
        String[] lines = rawTestCases.split("\\n");

//        for (String line : lines) {
//            try {
////                String testCase = mapper.readValue(line, String.class);
//                result.add(line);
//            } catch (Exception e) {
//                System.err.println("Failed to parse: " + line);
//            }
//        }
        if(lines.length < 1) {
            System.err.println("Failed to parse, No testCases Found");
        }

        return Arrays.stream(lines).toList();
    }

    public static List<String> extractOutputsFromContent(String htmlContent) {
        List<String> outputs = new ArrayList<>();

        // Normalize and split into lines
        String[] lines = htmlContent.replaceAll("<[^>]+>", "")  // strip HTML tags
                .replace("&nbsp;", " ")
                .split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Output:")) {
                String output = line.substring("Output:".length()).trim();
                outputs.add(output);
            }
        }

        return outputs;
    }

    public static String slugName(String name) {
        return name.trim().toLowerCase().replaceAll("\\s", "-");
    }
}
