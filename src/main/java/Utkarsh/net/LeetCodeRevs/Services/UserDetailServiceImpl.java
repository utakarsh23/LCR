package Utkarsh.net.LeetCodeRevs.Services;

import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailServiceImpl implements UserDetailsService { //Spring Security needs a way to get user data from your database,
    //This class acts like a bridge between user database and Spring Security’s authentication system.
    //Without it, Spring Security wouldn’t know how to find users or check their passwords.
    //So, this class basically tells Spring Security how to look up users during login.

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        /*	•	this function uses your UserRepository to fetch the user from the database by email.
            •	If user found:
                        It creates a Spring Security User object with username and password.
                        This User object is used by Spring Security to authenticate and manage security stuff.
            •	If user NOT found:
                        It throws UsernameNotFoundException, which tells Spring Security “No such user, deny access.”
        */
            User user = userRepository.findUserByEmail(email);
            if(user != null) {
                return org.springframework.security.core.userdetails.User.builder() //type User to get this
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .build();
            }
        throw new UsernameNotFoundException("No user found for the mail : " + email);
    }
}
