package Utkarsh.net.LeetCodeRevs.Services;

import Utkarsh.net.LeetCodeRevs.Entity.DbQuestions;
import Utkarsh.net.LeetCodeRevs.Repository.DbQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DbQuestionServices {

    @Autowired
    private DbQuestionRepository dbQuestionRepository;

    public boolean findBy(String name) {
        name = name.trim().toLowerCase().replaceAll("\\s", "-");
        try {
            DbQuestions byName = dbQuestionRepository.findByName(name);
            return byName.getName().equals(name);
        } catch (Exception ignored){
            return false;
        }
    }

}
