package Utkarsh.net.LeetCodeRevs.Controller;


import Utkarsh.net.LeetCodeRevs.DTO.LeetCodeProblem;
import Utkarsh.net.LeetCodeRevs.Entity.Questions;
import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Repository.QuestionRepository;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import Utkarsh.net.LeetCodeRevs.Services.LeetCodeService;
import Utkarsh.net.LeetCodeRevs.Services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private LeetCodeService leetCodeService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/postQues")
    public ResponseEntity<Questions> postSolution(@RequestBody Questions questionRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
            if(questionRequest.getQuestionLink() == null || questionRequest.getSolutions() == null || questionRequest.getSolutions().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }


        LeetCodeProblem leetCodeProblem = leetCodeService.fetchProblemData(questionRequest.getQuestionLink());
        questionRequest.setQuestionData(leetCodeProblem);
            questionRequest.setQuestionLink(questionRequest.getQuestionLink());
//            questionRequest.setSolutions(questionRequest.getSolutions());
        Questions questions = questionService.postSolution(questionRequest);

        User userByEmail = userRepository.findUserByEmail(email);
        if(userByEmail != null) {
            if(userByEmail.getQuestions() == null) {
                userByEmail.setQuestions(new ArrayList<>());
            }
            userByEmail.getQuestions().add(questionRequest);
            userRepository.save(userByEmail);
        }
        return new ResponseEntity<>(questions, HttpStatus.OK);
    }
}
