package ch.bbw.pr.tresorbackend.service.impl;

import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.repository.UserRepository;
import ch.bbw.pr.tresorbackend.service.UserService;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * UserServiceImpl
 * 
 * @author Peter Rutschmann
 */
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

   private UserRepository userRepository;

   @Override
   public User createUser(User user) {
      // Generate a salt for the new user
      SecureRandom random = new SecureRandom();
      byte[] saltBytes = new byte[16]; // 16 bytes = 128 bits, a common salt length
      random.nextBytes(saltBytes);
      user.setSalt(Base64.getEncoder().encodeToString(saltBytes));
      return userRepository.save(user);
   }

   @Override
   public User getUserById(Long userId) {
      Optional<User> optionalUser = userRepository.findById(userId);
      return optionalUser.get();
   }

   @Override
   public User findByEmail(String email) {
      return userRepository.findByEmail(email).orElse(null);
   }

   @Override
   public User findByResetPasswordToken(String token) {
      return userRepository.findByResetPasswordToken(token);
   }

   @Override
   public List<User> getAllUsers() {
      return (List<User>) userRepository.findAll();
   }

   @Override
   public User updateUser(User user) {
      User existingUser = userRepository.findById(user.getId()).get();
      existingUser.setFirstName(user.getFirstName());
      existingUser.setLastName(user.getLastName());
      existingUser.setEmail(user.getEmail());
      existingUser.setPassword(user.getPassword());
      existingUser.setResetPasswordToken(user.getResetPasswordToken());
      existingUser.setResetPasswordTokenExpiry(user.getResetPasswordTokenExpiry());
      User updatedUser = userRepository.save(existingUser);
      return updatedUser;
   }

   @Override
   public void deleteUser(Long userId) {
      userRepository.deleteById(userId);
   }
}
