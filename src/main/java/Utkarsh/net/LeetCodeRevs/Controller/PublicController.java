package Utkarsh.net.LeetCodeRevs.Controller;


import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import Utkarsh.net.LeetCodeRevs.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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

    @Autowired
    private CacheManager cacheManager;


    @GetMapping
    public String health() { //healthCheck API
        System.out.println("publichealth");
        return "It's working";
    }

    @PostMapping("/login") //for login
    public ResponseEntity<?> login(@RequestBody User loginRequest, HttpServletRequest request) {
        //User loginRequest -> changes the coming JSON data into the User Object,
        //HttpServletRequest -> basically contains client side data such as headers, JWT, cookies, Session info, HTTP Methods(GET/POST/PUT/DEL..), URL's and such user thingys

        String clientIp = getClientIP(request);
        System.out.println("Login attempt from IP: " + clientIp);
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

    @GetMapping("/cache") //just random to print the Cache inside the caffein cache manager
    public void printCache() {
        printLeetcodeLinksCache();
    }

    public void printLeetcodeLinksCache() {
        Cache leetcodeLinksCache = cacheManager.getCache("leetcodeLinks");
        System.out.println("cache printing");
        if (leetcodeLinksCache != null) {
            Object nativeCache = leetcodeLinksCache.getNativeCache();
            if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache) {
                com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache =
                        (com.github.benmanes.caffeine.cache.Cache<?, ?>) nativeCache;

                caffeineCache.asMap().forEach((key, value) -> {
                    System.out.println("Cache Key: " + key + ", Value: " + value);
                });
            } else {
                System.out.println("Native cache is not Caffeine, it's: " + nativeCache.getClass().getName());
            }
        } else {
            System.out.println("Cache 'leetcodeLinks' not found!");
        }
    }

    public String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr(); // fallback if no proxy
        }
        return xfHeader.split(",")[0]; // sometimes it's a list: client, proxy1, proxy2...
    }
}
