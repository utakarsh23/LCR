package Utkarsh.net.LeetCodeRevs.Controller;


import Utkarsh.net.LeetCodeRevs.DTO.LeetCodeProblem;
import Utkarsh.net.LeetCodeRevs.Entity.Questions;
import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Repository.QuestionRepository;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import Utkarsh.net.LeetCodeRevs.Services.GeminiService;
import Utkarsh.net.LeetCodeRevs.Services.LeetCodeService;
import Utkarsh.net.LeetCodeRevs.Services.QuestionService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private LeetCodeService leetCodeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/postQues") //this is for the first time solution submission until it gets automated
    public ResponseEntity<Questions> postSolution(@RequestBody Questions questionRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findUserByEmail(email);
            if(questionRequest.getQuestionLink() == null || questionRequest.getSolutions() == null || questionRequest.getSolutions().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

        LeetCodeProblem leetCodeProblem = leetCodeService.fetchProblemData(questionRequest.getQuestionLink());
        questionRequest.setQuestionData(leetCodeProblem);
            questionRequest.setQuestionLink(questionRequest.getQuestionLink());
        questionRequest.setUser(user);
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

    @GetMapping("/random") //simple to get a random question when fetched(scheduling will be handled later)
    public Questions getRandomQuestion() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findUserByEmail(email);
        List<Questions> questions = questionRepository.findQuestionsByUser(user);

        if (questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No questions found for the user.");
        }

        Random random = new Random();
        return questions.get(random.nextInt(questions.size()));
    }

    @PostMapping("/postSolution")
    public ResponseEntity<?> postDailySolution(@RequestBody String input) {
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

//        String questionTitle = (String) user.getDailyQuestion().subSequence(9, user.getDailyQuestion().indexOf("Difficulty") - 1);
//        questionRepository.findQuestionsByQuestionName(questionTitle);
        //
        int text = geminiService.askGemini(input, content, testCases).indexOf("text");
        CharSequence charSequence = geminiService.askGemini(input, content, testCases).subSequence(text + 7, text + 16); //hard code lol
        return new ResponseEntity<>(charSequence,HttpStatus.OK);
    }

    @Scheduled(cron = "030 04 02 * * ?")
    public void assignRandomQuestionToUsers() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            List<Questions> userQuestions = user.getQuestions(); // Get only this user's questions

            if (userQuestions != null && !userQuestions.isEmpty()) {
                Questions randomQuestion = getRandomQuestion(userQuestions);
                ObjectId id = randomQuestion.getId();
                if (randomQuestion != null) {
                    user.setDailyQuestion(randomQuestion.getQuestionData() + "");
                    user.setDailyQuesID(id);
                    userRepository.save(user);
                    System.out.println("Assigned '" + randomQuestion.getQuestionData().getTitle() +
                            "' to user: " + user.getEmail());
                } else {
                    System.out.println("No valid questions found for user: " + user.getEmail());
                }
            } else {
                System.out.println("User " + user.getEmail() + " has no questions.");
            }
        }
    }

    /**
     * Helper method to get a random question from a given list.
     */
    private Questions getRandomQuestion(List<Questions> questions) {
        if (questions.isEmpty()) return null;
        Random random = new Random();
        return questions.get(random.nextInt(questions.size()));
    }
}
