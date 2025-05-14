package Utkarsh.net.LeetCodeRevs.Controller;

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

import java.util.List;

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
    private ResponseEntity<User> findUserByEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findUserByEmail(email);
        List<String> questionTitles = leetCodeService.getQuestionTitles(user.getLeetCodeUserName());
        List<String> submissions = userService.findUserByEmail(email).getSubmissions();
        for (int i = 0; i < questionTitles.size(); i++) {
            if(!submissions.contains(questionTitles.get(i)))
                submissions.add(0, questionTitles.get(i));
            else
                break;
        }
        user.setSubmissions(submissions);
        userRepository.save(user);
        return new ResponseEntity<>(userService.findUserByEmail(email), HttpStatus.OK);
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
