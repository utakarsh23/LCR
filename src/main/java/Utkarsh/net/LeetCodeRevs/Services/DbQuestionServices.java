package Utkarsh.net.LeetCodeRevs.Services;

import Utkarsh.net.LeetCodeRevs.Entity.DbQuestions;
import Utkarsh.net.LeetCodeRevs.Repository.DbQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DbQuestionServices {

    @Autowired
    private DbQuestionRepository dbQuestionRepository;

    public boolean findBy(String name) { //finding the question in the db with boolean return to get inside method
        name = name.trim().toLowerCase().replaceAll("\\s", "-"); //for matching the slug type question name in fb
        try {
            DbQuestions byName = dbQuestionRepository.findByName(name);
            return byName.getName().equals(name);
        } catch (Exception ignored){
            return false; //returning false when user == null as we haven't used if else for checking nullability
        }
    }

}
