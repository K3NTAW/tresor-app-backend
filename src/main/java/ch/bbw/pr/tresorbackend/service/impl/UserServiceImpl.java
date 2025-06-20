package ch.bbw.pr.tresorbackend.service.impl;

import ch.bbw.pr.tresorbackend.model.RegisterUser;
import ch.bbw.pr.tresorbackend.model.Role;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.repository.UserRepository;
import ch.bbw.pr.tresorbackend.service.RecaptchaService;
import ch.bbw.pr.tresorbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RecaptchaService recaptchaService;

    @Override
    public User registerUser(RegisterUser registerUser) {
        if (!recaptchaService.verifyRecaptcha(registerUser.getRecaptchaToken())) {
            throw new IllegalStateException("reCAPTCHA verification failed");
        }
        if (userRepository.findByEmail(registerUser.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already in use");
        }

        if (!registerUser.getPassword().equals(registerUser.getPasswordConfirmation())) {
            throw new IllegalStateException("Passwords do not match");
        }

        User newUser = new User();
        newUser.setFirstName(registerUser.getFirstName());
        newUser.setLastName(registerUser.getLastName());
        newUser.setEmail(registerUser.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerUser.getPassword()));
        
        // Generate a salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        newUser.setSalt(Base64.getEncoder().encodeToString(salt));
        
        newUser.getRoles().add(Role.USER);

        return userRepository.save(newUser);
    }
    
    @Override
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Generate a salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        user.setSalt(Base64.getEncoder().encodeToString(salt));
        
        user.getRoles().add(Role.USER);
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public User findByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token);
    }
} 