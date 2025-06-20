package ch.bbw.pr.tresorbackend.service;

import ch.bbw.pr.tresorbackend.model.RegisterUser;
import ch.bbw.pr.tresorbackend.model.User;
import java.util.List;

public interface UserService {
    User registerUser(RegisterUser registerUser);
    User createUser(User user);
    User getUserById(Long userId);
    User findByEmail(String email);
    List<User> getAllUsers();
    User updateUser(User user);
    void deleteUser(Long userId);
    User findByResetPasswordToken(String token);
}
