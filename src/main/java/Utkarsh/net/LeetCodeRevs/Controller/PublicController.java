package Utkarsh.net.LeetCodeRevs.Controller;


import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import Utkarsh.net.LeetCodeRevs.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager; //authentication manager for login and stuffs management

    @GetMapping
    public String health() { //healthCheck API
        System.out.println("publichealth");
        return "It's working";
    }

    @PostMapping("/login") //for login
    public ResponseEntity<?> login(@RequestBody User loginRequest, HttpServletRequest request) {
        //User loginRequest -> changes the coming JSON data into the User Object,
        //HttpServletRequest -> basically contains client side data such as headers, JWT, cookies, Session info, HTTP Methods(GET/POST/PUT/DEL..), URL's and such user thingys
        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
            //to create the authToken and the token is used as creal container for sprig security

            var authResult = authenticationManager.authenticate(authToken);
            //used for validating and checking the credentials inside the token and if matched then create and gives an authenticated object with user details

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext(); //a new context Holder as the name suggests
            securityContext.setAuthentication(authResult); //to hold the auth info of the current session/request ////termed as user got logged in
            request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", securityContext); //The security context is stored inside the user’s HTTP session so that Spring Security can track the user’s logged-in state across requests.
            //request -> the holding info of user thingy,
            //getSession(true) -> gets current session, if not present creates a new one,
            //setAttribute("blah blah", securityContext(holds the session data/auth info)) -> Spring Security uses this key("SPRING_SECURITY_CONTEXT") to keep track of the authenticated user’s security info (like who is logged in) throughout the session.
            //by saving it all here we are making the user creds as logged in so he can be authenticated as the creds of already logged in are saved in the user data/token/cookies or whatever

            System.out.println("loggedin"); // a debugger 😝
            return ResponseEntity.ok("Login successful");

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/signup")
    private ResponseEntity<?> createNewUsers(@RequestBody User user) {
        if(userRepository.existsByEmail(user.getEmail())) {
            return new ResponseEntity<>("User already exists", HttpStatus.I_AM_A_TEAPOT);
        }
        userService.createUser(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}
