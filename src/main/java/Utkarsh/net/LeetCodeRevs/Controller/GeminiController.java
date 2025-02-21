package Utkarsh.net.LeetCodeRevs.Controller;

import Utkarsh.net.LeetCodeRevs.Entity.Questions;
import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Repository.QuestionRepository;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import Utkarsh.net.LeetCodeRevs.Services.GeminiService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @PostMapping("/ask")
    public ResponseEntity<?> askGemini(@RequestBody String input) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findUserByEmail(email);
        ObjectId dailyQuestionId = user.getDailyQuesID(); //questionId

//        System.out.println(dailyQuestionId);
        if (dailyQuestionId == null) {
            return new ResponseEntity<>("No daily questions assigned!", HttpStatus.BAD_REQUEST);
        }
        List<Questions> questionsById = questionRepository.getQuestionsById(dailyQuestionId);
        String content = questionsById.get(0).getQuestionData().getContent();
        String testCases = questionsById.get(0).getQuestionData().getExampleTestcases();

        return new ResponseEntity<>(geminiService.askGemini(input, content, testCases), HttpStatus.OK);
    }

}
