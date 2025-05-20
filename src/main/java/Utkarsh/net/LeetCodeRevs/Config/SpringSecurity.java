package Utkarsh.net.LeetCodeRevs.Config;

import Utkarsh.net.LeetCodeRevs.Services.UserDetailServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

//ahh here comes the most tough thing springBoot has to offer
@Configuration
@EnableWebSecurity
public class SpringSecurity {

    private final UserDetailServiceImpl userDetailService;

    public SpringSecurity(UserDetailServiceImpl userDetailService) {
        this.userDetailService = userDetailService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { //using SecurityFilterChain
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for APIs
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll() // Public APIs (signup, login, health)
                        .requestMatchers("/user/**").authenticated() // Secure user APIs
                        .requestMatchers("/ws/**").permitAll() //public for notifications
                )
                // Removed httpBasic() to disable Basic Auth
                //.httpBasic(Customizer.withDefaults())

                // Enabling session management (to create session if required)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .build(); //build it
    }

    @Bean
    public AuthenticationManager authenticationManager() { //auth manager for login and managing stuffs, it's usedd to decide if login got success or not
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(); //it's built in SB thing for checking auths directly from th db by retrieving email, pass and such details or roles whatever needed
        authProvider.setUserDetailsService(userDetailService); //as mentioned, this one loads/fetches the email n pass from teh db
        authProvider.setPasswordEncoder(passwordEncoder()); //checks for if the password is correct or not as password is hashed so it hashes and matches the hashed pass from db, compares it and yeah that's it
        return new ProviderManager(authProvider);
        /*as authentication Manager is just an interface we give it a real implementation by ProviderManager(Can be one or more depending on auth),
        it basically does is when a login attempt happens, it makes SB use this auth provider (which checks email + hashed password in DB) which we did and checked with authprovider above,, ahh somuch explanation */
    }

    //hmm so we are meddling with CORS - Cross origin Resource Sharing are we(worst thing happened to humanity)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() { //method for policy and rules for connection with the frontend
        CorsConfiguration configuration = new CorsConfiguration(); //creating a corsConfig object
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); //allowing which-which origins can acces this backend
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); //and methods they can use
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Origin", "Accept")); //type of headers we can allow for frontend to backend connections
        configuration.setExposedHeaders(List.of("Set-Cookie"));
        configuration.setAllowCredentials(true); //it basically allows cookies, authorization headers, or TLS client certificates to be included inside the requests
                                                 //Required for withCredentials: true in Axios or Fetch.

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); //sees through which url's these configs are applies
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() { //for password hashing
        return new BCryptPasswordEncoder(); // Password encoder
    }
}