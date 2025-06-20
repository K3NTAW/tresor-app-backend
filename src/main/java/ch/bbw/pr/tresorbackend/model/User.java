package ch.bbw.pr.tresorbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

   @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
   @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
   @Enumerated(EnumType.STRING)
   @Column(name = "role")
   private Set<Role> roles = new HashSet<>();

   public Long getId() {
       return id;
   }

   public void setId(Long id) {
       this.id = id;
   }

   public String getFirstName() {
       return firstName;
   }

   public void setFirstName(String firstName) {
       this.firstName = firstName;
   }

   public String getLastName() {
       return lastName;
   }

   public void setLastName(String lastName) {
       this.lastName = lastName;
   }

   public String getEmail() {
       return email;
   }

   public void setEmail(String email) {
       this.email = email;
   }

   public String getPassword() {
       return password;
   }

   public void setPassword(String password) {
       this.password = password;
   }

   public String getSalt() {
       return salt;
   }

   public void setSalt(String salt) {
       this.salt = salt;
   }

   public String getResetPasswordToken() {
       return resetPasswordToken;
   }

   public void setResetPasswordToken(String resetPasswordToken) {
       this.resetPasswordToken = resetPasswordToken;
   }

   public Date getResetPasswordTokenExpiry() {
       return resetPasswordTokenExpiry;
   }

   public void setResetPasswordTokenExpiry(Date resetPasswordTokenExpiry) {
       this.resetPasswordTokenExpiry = resetPasswordTokenExpiry;
   }

   public Set<Role> getRoles() {
       return roles;
   }

   public void setRoles(Set<Role> roles) {
       this.roles = roles;
   }
}
