package ch.bbw.pr.tresorbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;

/**
 * User
 * 
 * @author Peter Rutschmann
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(nullable = false, name = "first_name")
   private String firstName;

   @Column(nullable = false, name = "last_name")
   private String lastName;

   @Column(nullable = false, unique = true)
   private String email;

   @Column(nullable = false)
   private String password;

   @Column(nullable = true) // Initially nullable for existing users, will be set for new users
   private String salt;

   @Column(name = "reset_password_token")
   private String resetPasswordToken;

   @Column(name = "reset_password_token_expiry")
   @Temporal(TemporalType.TIMESTAMP)
   private Date resetPasswordTokenExpiry;
}
