package Utkarsh.net.LeetCodeRevs.Services;

import Utkarsh.net.LeetCodeRevs.Entity.User;
import Utkarsh.net.LeetCodeRevs.Repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); //to hash the password


    public void createUser(User user) {
        user.setEmail(user.getEmail());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setLeetCodeUserName(user.getLeetCodeUserName());
        userRepository.save(user);
    }

    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }
}
