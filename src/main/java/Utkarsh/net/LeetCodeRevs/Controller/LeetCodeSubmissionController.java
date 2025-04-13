package Utkarsh.net.LeetCodeRevs.Controller;

import Utkarsh.net.LeetCodeRevs.Entity.LeetCodeSubmissions;
import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import Utkarsh.net.LeetCodeRevs.Services.LeetCodeSubmissionServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
public class LeetCodeSubmissionController {

    @Autowired
    private LeetCodeSubmissionServices leetCodeSubmissionServices;

    @Autowired
    private UserRepository userRepository;


    @PostMapping("/saveSol")
    public ResponseEntity<?> saveSolution(@RequestBody LeetCodeSubmissions leetCodeSubmissions) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user found with email: " + email);
        }

        user.setLeetCodeSubmissions(leetCodeSubmissions);
        userRepository.save(user);

        Object response = leetCodeSubmissionServices.leetCodeSubmission(leetCodeSubmissions);
        return ResponseEntity.ok(response);
    }
}
