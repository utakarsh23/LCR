package Utkarsh.net.LeetCodeRevs.Services;

import Utkarsh.net.LeetCodeRevs.Entity.LeetCodeSubmissions;
import Utkarsh.net.LeetCodeRevs.Repository.LeetcodeSubmissionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LeetCodeSubmissionServices {

    @Autowired
    private LeetcodeSubmissionsRepository leetcodeSubmissionsRepository;

    public LeetCodeSubmissions leetCodeSubmission(LeetCodeSubmissions leetCodeSubmissions) {
            leetCodeSubmissions.setId(leetCodeSubmissions.getId());
            leetCodeSubmissions.setLink(leetCodeSubmissions.getLink());
            leetCodeSubmissions.setQuestionNumber(leetCodeSubmissions.getQuestionNumber());
            leetCodeSubmissions.setQuestionTitle(leetCodeSubmissions.getQuestionTitle());
            leetCodeSubmissions.setSubmittedBy(leetCodeSubmissions.getSubmittedBy());
            leetCodeSubmissions.setTopics(leetCodeSubmissions.getTopics());
        return leetcodeSubmissionsRepository.save(leetCodeSubmissions);
    }
}
