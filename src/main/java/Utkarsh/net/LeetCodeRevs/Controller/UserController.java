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

    @GetMapping("/cli/daily-question") //method used for the cli part of the application to get the daily question link and the testCases for it
    public ResponseEntity<Map<String, List<TestCase>>> getDailyQuestion() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); //another way for authentication
        String email = authentication.getName(); //getting name(email in the case)
        //as we already saved the authentication state the SecurityContextHolder, we are just taking back the credentials again from it
        //ScHolder -> holds the user side details //getContext -> gives the details
        //.getAUthentication -> holds the details of the user from the auths


        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Map<String, List<TestCase>> response = new HashMap<>(); //we are returning the link of the question and TestCases so Map<"link", TestCases>
        //using List<TestCases> as there more than 1 testCase for each question

        String dqLink = user.getDailyAssignedQuestionLink(); //dqLink = daily question link of normal type question
        if (dqLink != null) {
            UserQuestionData dqData = user.getUserQuestions().values().stream()
                    .filter(q -> dqLink.equals(q.getLink()))
                    .findFirst()
                    .orElse(null);
            // extracting the question data of the question using the link

            if (dqData != null && dqData.getTestCase() != null) {
                response.put(dqLink, dqData.getTestCase()); //adding the link and List of TestCases
            }
        }

        String tqLink = user.getDailyAssignedTopicQuestionLink(); //tqLink = daily question link of the Topic wise assigned question
        if (tqLink != null) {
            UserQuestionData tqData = user.getUserQuestions().values().stream()
                    .filter(q -> tqLink.equals(q.getLink()))
                    .findFirst()
                    .orElse(null);
            // extracting the question data of the question using the link


            if (tqData != null && tqData.getTestCase() != null) {
                response.put(tqLink, tqData.getTestCase()); //adding the link and List of TestCases
            }
        }

        return ResponseEntity.ok(response);
    }

//    private ResponseEntity<Map<String, Map<String, Object>>> buildErrorResponse(String errorMsg) {
//        Map<String, Object> innerResponse = new HashMap<>();
//        innerResponse.put("error", errorMsg);
//        innerResponse.put("dailyQuestionLink", null);
//        innerResponse.put("topicQuestionLink", null);
//
//        Map<String, Map<String, Object>> outerResponse = new HashMap<>();
//        outerResponse.put("response", innerResponse);
//
//        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(outerResponse);
//    }

    //prolly the worst code ever
    @GetMapping("getProfile")
    public ResponseEntity<Object> findUserByEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); //auths stuffs, explained above
        String email = authentication.getName();

        System.out.println("yayyy");
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Map<String, UserQuestionData> userQuestions = user.getUserQuestions(); //to get the user questions from the db so no existing questions get added inside the db and also to limit the api by not checking if the question exists already
        if (userQuestions == null) {
            userQuestions = new HashMap<>();
        }

        List<String> questionTitles;
        try {
            questionTitles = leetCodeService.getQuestionTitles(user.getLeetCodeUserName()); //getching the api for last 20 correct submissions, returning it into a List of Strings in one go
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


        for (String title : questionTitles) { //name of question = title
            if (!userQuestions.containsKey(title)) { //checking if exists in db
                try {
                    String linkOfQuestion = leetCodeService.fetchLeetcodeLink(title); //another api call for getting link by using the name
                    List<String> questionTags;
                    List<TestCase> testCase = new ArrayList<>();
                    boolean existsInDb = dbQuestionServices.findBy(title); //checks if the question exists in the db or not
                    String slugTitle = slugName(title); //slugTitle = name but in db format i.e title = "Two Sum", slugTitle = "two-sum"
                    //we are checking in db to avoid another webScrapping that can possible happen if there's no question found in db with the same name as the new question arrives on LC weekly taking edge cases into consideration
                    if(existsInDb) { //if true ---
                        DbQuestions dbQuestions = dbQuestionRepository.findByName(slugTitle);
                        questionTags = dbQuestions.getTags(); //tags from the db
                        testCase = dbQuestions.getTestcases(); //testcases from the db
                    } else { //false
                        LeetCodeProblem leetCodeProblem = leetCodeService.fetchProblemData(linkOfQuestion); //scrapping web for the data
                        questionTags = leetCodeProblem.getTopicTags();
                        List<String> testCasesList = parseStringTestCases(leetCodeProblem.getExampleTestcases());
                                //parsing test cases "leetCodeProblem.getExampleTestcases() = { s = "[1, 2, 3]"\nt = "[4, 5, 7]"} and parsing it makes a list of both s = [1, 2, 3] & t = [4, 5 7]"
                        List<String> testCaseOutput = extractOutputsFromContent(leetCodeProblem.getContent()); //html to outputs extraction
                        for (int i = 0; i < Math.min(testCasesList.size(), testCaseOutput.size()); i++) { //adding testCases
                            testCase.add(new TestCase(testCasesList.get(i), testCaseOutput.get(i)));
                        }
                    }

                    UserQuestionData questionData = new UserQuestionData();
                    questionData.setTitle(title);
                    questionData.setLink(linkOfQuestion);
                    questionData.setTags(questionTags);
                    questionData.setTestCase(testCase);
                    questionData.setWeight(1.0); //setting newly added question's weight as 1

                    userQuestions.put(title, questionData); //adding the name and question into the map to save in db later
                } catch (Exception e) {
                    //in case api failes, it still saves the fetched ques
                    user.setUserQuestions(userQuestions);
                    userRepository.save(user);
                    return ResponseEntity.badRequest().body("Failed to fetch link or tags for title: " + title + " - " + e.getMessage());
                }
            }
        }

        //a method for updating weight, used here to update the weights while user goes to the profile for real time data updation
        dailyUpdateQuestionsAndWeightService.updateWeightsForSubmissions(user, userQuestions);

        user.setUserQuestions(userQuestions); //saving ques to db
        userRepository.save(user); //need (took 30mins to find this missing)

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

    //used just in case if we have to scrap the testcases whenever the testCases(Question) are not available inside the db(datasheet)
    public static List<String> parseStringTestCases(String rawTestCases) {
        String[] lines = rawTestCases.split("\\n");

        if(lines.length < 1) {
            System.err.println("Failed to parse, No testCases Found");
        }

        return Arrays.stream(lines).toList();
    }

    //to extract output from the html contents(code) while scraping the web while output(Question) isn't available inside the (db)datasheet
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

    //to convert normal names into making them similar to as names in db
    public static String slugName(String name) {
        return name.trim().toLowerCase().replaceAll("\\s", "-");
    }

//    @CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
//    @GetMapping("/check")
//    public ResponseEntity<String> checkAuth(HttpServletRequest request) {
//        if (request.getCookies() != null) {
//            Arrays.stream(request.getCookies())
//                    .forEach(cookie -> System.out.println("Cookie: " + cookie.getName() + " = " + cookie.getValue()));
//        } else {
//            System.out.println("No cookies received");
//        }
//
//        return ResponseEntity.ok("Authenticated");
//    }
}
