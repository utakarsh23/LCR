package Utkarsh.net.LeetCodeRevs.Controller;

import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Entity.UserQuestionData;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import Utkarsh.net.LeetCodeRevs.Services.QuestionServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
//not mendatorily used, created for debugging and stuffs
@RestController
@RequestMapping("/dailyQues")
public class DailyQuestionsController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/byQuestionType")
    public ResponseEntity<?> getDailyQuestionByQuestionType(@AuthenticationPrincipal UserDetails userDetails) { //AuthenticationPrincipal is to get/inject currently authenticated user details such as email or whatever using headers or seesion or whatever method(UserDetail in this case)
        String email = userDetails.getUsername();
        User user = userRepository.findUserByEmail(email);
        if (user == null)
            return ResponseEntity.badRequest().body("Can not find User for this mail " + email);

        return new ResponseEntity<>(user.getDailyAssignedQuestionLink(), HttpStatus.OK);
    }


    @GetMapping("/byTopicType")
    public ResponseEntity<?> getDailyQuestionByTopicType(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findUserByEmail(email);
        if (user == null)
            return ResponseEntity.badRequest().body("Can not find User for this mail " + email);

        String link = user.getDailyAssignedQuestionLink();

        // Find the question data based on the link (reverse match)
        UserQuestionData questionData = user.getUserQuestions().values().stream()
                .filter(q -> q.getLink().equals(link))
                .findFirst()
                .orElse(null);

        if (questionData == null) {
            return ResponseEntity.badRequest().body("Question data not found for assigned link");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("link", link);
        response.put("topics", questionData.getTags());

        return ResponseEntity.ok(response); //returns both quesLink and tags as the methodName suggests
    }
}
