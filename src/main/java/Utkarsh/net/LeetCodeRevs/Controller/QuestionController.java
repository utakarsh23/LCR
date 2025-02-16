package Utkarsh.net.LeetCodeRevs.Controller;


import Utkarsh.net.LeetCodeRevs.DTO.QuestionRequestDTO;
import Utkarsh.net.LeetCodeRevs.Entity.Questions;
import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Repository.QuestionRepository;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;


    @PostMapping("/postQues")
    public ResponseEntity<Questions> postSolution(@RequestBody Questions questionRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
            if(questionRequest.getQuestionLink() == null || questionRequest.getSolutions() == null) {
                return ResponseEntity.badRequest().build();
            }
            questionRequest.setQuestionLink(questionRequest.getQuestionLink());
            questionRequest.setSolutions(questionRequest.getSolutions());
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
