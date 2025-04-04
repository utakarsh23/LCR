package Utkarsh.net.LeetCodeRevs.Controller;


import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import Utkarsh.net.LeetCodeRevs.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @GetMapping
    public String health() { //healthCheck API
        return "It's working";
    }

    @PostMapping("/signup")
    private ResponseEntity<?> createNewUsers(@RequestBody User user) { //to create the new user
        if(userRepository.existsByEmail(user.getEmail())) {
            return new ResponseEntity<>("User already exists", HttpStatus.I_AM_A_TEAPOT);
        }
        userService.createUser(user); //creating the user if no email exists for such
        return new ResponseEntity<>(user, HttpStatus.CREATED); //returning the user
    }
}
