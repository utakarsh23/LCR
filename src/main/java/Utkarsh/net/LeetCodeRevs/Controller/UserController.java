package Utkarsh.net.LeetCodeRevs.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Utkarsh.net.LeetCodeRevs.Entity.Submission;
import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import Utkarsh.net.LeetCodeRevs.Services.LeetCodeService;
import Utkarsh.net.LeetCodeRevs.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LeetCodeService leetCodeService;

    @GetMapping("getProfile")
    private ResponseEntity<Object> findUserByEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findUserByEmail(email);

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Map<String, String> submissions = user.getSubmissions();
        if (submissions == null) {
            submissions = new HashMap<>();
        }

        Set<String> submissionSet = new HashSet<>(submissions.keySet());
        List<String> questionTitles;
        try {
            questionTitles = leetCodeService.getQuestionTitles(user.getLeetCodeUserName());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to fetch question titles from LeetCode API : " + e);
        }

        for (String title : questionTitles) {
            if (!submissionSet.contains(title)) {
                try {
                    String linkOfQuestion = leetCodeService.fetchLeetcodeLink(title);
                    submissions.put(title, linkOfQuestion);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("Failed to fetch link for title: {}" + title + e);
                }
            }
        }


        System.out.println("lmaooosssss");

        user.setSubmissions(submissions);
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/deleteUser")
    private ResponseEntity<Boolean> deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findUserByEmail(email);
        if (email != null && !email.isEmpty()) {
            userRepository.deleteById(user.getId());
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
    }

    @PutMapping("/updateUser")
    private ResponseEntity<?> updateUser(@RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User userInDb = userRepository.findUserByEmail(email);
            userInDb.setEmail(user.getEmail());
            userInDb.setPassword(user.getPassword());
            userInDb.setLeetCodeUserName(user.getLeetCodeUserName());
            userService.createUser(userInDb);
            return new ResponseEntity<>(user,HttpStatus.FOUND);
    }

    @PostMapping("/signout")
    public void signOut() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        //wait
    }
}
