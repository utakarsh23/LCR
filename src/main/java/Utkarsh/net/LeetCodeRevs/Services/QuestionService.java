package Utkarsh.net.LeetCodeRevs.Services;

import Utkarsh.net.LeetCodeRevs.Entity.Questions;
import Utkarsh.net.LeetCodeRevs.Repository.QuestionRepository;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;


    public Questions postSolution(Questions questions) {
            questions.setQuestionLink(questions.getQuestionLink());
            String nameOfQues = "";
                if(questions.getQuestionLink().contains("leetcode.com/problems/")) {
                    String[] parts = questions.getQuestionLink().split("leetcode.com/problems/");
                    if (parts.length > 1) {
                        nameOfQues = parts[1].replaceAll("-", " ").replaceAll("/", "").trim();
                    }
                } else {
                    return null;
                }
                questions.setSolutions(questions.getSolutions());
                questions.setQuestionName(nameOfQues);
            return questionRepository.save(questions);
    }

    public Questions getRandomQuestion() {
        return questionRepository.findRandomQuestion().orElse(null);
    }
}
