package Utkarsh.net.LeetCodeRevs.Controller;

import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Entity.UserQuestionData;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import Utkarsh.net.LeetCodeRevs.Services.DailyUpdateQuestionsAndWeightService;
import Utkarsh.net.LeetCodeRevs.Services.LeetCodeService;
import Utkarsh.net.LeetCodeRevs.Services.UserService;
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
    private DailyUpdateQuestionsAndWeightService dailyUpdateQuestionsAndWeightService;


    //prolly the worst code ever
    @GetMapping("getProfile")
    public ResponseEntity<Object> findUserByEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Initialize userQuestions map if null
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
            return ResponseEntity.badRequest().body("Failed to fetch question titles from LeetCode API: " + e.getMessage());
        }


        for (String title : questionTitles) {
            // Check if question already exists by title (you may want to map title -> questionId here)
            // Since keys are questionIds, you might need to fetch questionId from title or elsewhere
            // For now, assuming title is unique and used as questionId
            if (!userQuestions.containsKey(title)) {
                try {
                    String linkOfQuestion = leetCodeService.fetchLeetcodeLink(title);
                    List<String> questionTags = leetCodeService.fetchProblemData(linkOfQuestion).getTopicTags();

                    // Create new UserQuestionData with default weight
                    UserQuestionData questionData = new UserQuestionData();
                    questionData.setTitle(title);
                    questionData.setLink(linkOfQuestion);
                    questionData.setTags(questionTags);
                    questionData.setWeight(1.0);              // default initial weight

                    userQuestions.put(title, questionData);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("Failed to fetch link or tags for title: " + title + " - " + e.getMessage());
                }
            }
        }
        dailyUpdateQuestionsAndWeightService.updateWeightsForSubmissions(user, userQuestions);

        user.setUserQuestions(userQuestions);
        userRepository.save(user);

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
}
