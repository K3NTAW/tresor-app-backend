package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.model.Secret;
import ch.bbw.pr.tresorbackend.model.NewSecret;
import ch.bbw.pr.tresorbackend.model.EncryptCredentials;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.model.SecretDTO;
import ch.bbw.pr.tresorbackend.service.SecretService;
import ch.bbw.pr.tresorbackend.service.UserService;
import ch.bbw.pr.tresorbackend.util.EncryptUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.validation.Valid;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SecretController
 * 
 * @author Peter Rutschmann
 */
@RestController
@RequestMapping("api/secrets")
public class SecretController {

   private SecretService secretService;
   private UserService userService;
   private String secretPepper;

   public SecretController(SecretService secretService, UserService userService, @Value("${app.security.secret-pepper}") String secretPepper) {
      this.secretService = secretService;
      this.userService = userService;
      this.secretPepper = secretPepper;
   }

   // create secret REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping
   public ResponseEntity<String> createSecret2(@Valid @RequestBody NewSecret newSecret, BindingResult bindingResult) {
      // input validation
      if (bindingResult.hasErrors()) {
         List<String> errors = bindingResult.getFieldErrors().stream()
               .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
               .collect(Collectors.toList());
         System.out.println("SecretController.createSecret " + errors);

         JsonArray arr = new JsonArray();
         errors.forEach(arr::add);
         JsonObject obj = new JsonObject();
         obj.add("message", arr);
         String json = new Gson().toJson(obj);

         System.out.println("SecretController.createSecret, validation fails: " + json);
         return ResponseEntity.badRequest().body(json);
      }
      System.out.println("SecretController.createSecret, input validation passed");

      User user = userService.findByEmail(newSecret.getEmail());
      if (user == null || user.getSalt() == null) {
         // Handle case where user or salt is not found (should not happen for new users)
         System.out.println("SecretController.createSecret, user or salt not found for email: " + newSecret.getEmail());
         JsonObject obj = new JsonObject();
         obj.addProperty("message", "User or salt not found, cannot create secret.");
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Gson().toJson(obj));
      }

      // transfer secret and encrypt content
      Secret secret = new Secret(
            null,
            user.getId(),
            new EncryptUtil(newSecret.getEncryptPassword(), user.getSalt(), secretPepper).encrypt(newSecret.getContent().toString()));
      // save secret in db
      secretService.createSecret(secret);
      System.out.println("SecretController.createSecret, secret saved in db");
      JsonObject obj = new JsonObject();
      obj.addProperty("answer", "Secret saved");
      String json = new Gson().toJson(obj);
      System.out.println("SecretController.createSecret " + json);
      return ResponseEntity.accepted().body(json);
   }

   // Build Get Secrets by userId REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping("/byuserid")
   public ResponseEntity<List<SecretDTO>> getSecretsByUserId(@RequestBody EncryptCredentials credentials) {
      System.out.println("SecretController.getSecretsByUserId " + credentials);

      List<Secret> secrets = secretService.getSecretsByUserId(credentials.getUserId());
      if (secrets.isEmpty()) {
         System.out.println("SecretController.getSecretsByUserId secret isEmpty");
         return ResponseEntity.notFound().build();
      }
      // Decrypt content
      ObjectMapper objectMapper = new ObjectMapper();
      List<SecretDTO> secretDTOs = new java.util.ArrayList<>();
      for (Secret secret : secrets) {
         String decryptedContent;
         try {
            // Need to get the user associated with these secrets to get their salt
            // Assuming credentials.getUserId() gives the correct user id for ALL these
            // secrets.
            User secretOwner = userService.getUserById(credentials.getUserId());
            if (secretOwner == null || secretOwner.getSalt() == null) {
               System.out.println("SecretController.getSecretsByUserId, owner or salt not found for user ID: "
                     + credentials.getUserId());
               decryptedContent = "{\"error\":\"Owner or salt not found for decryption.\"}";
            } else {
               decryptedContent = new EncryptUtil(credentials.getEncryptPassword(), secretOwner.getSalt(), secretPepper)
                  .decrypt(secret.getContent());
            }
         } catch (EncryptionOperationNotPossibleException e) {
            System.out.println("SecretController.getSecretsByUserId " + e + " " + secret);
            decryptedContent = "{\"error\":\"not encryptable. Wrong password?\"}";
         }
         
         try {
            JsonNode contentNode = objectMapper.readTree(decryptedContent);
            secretDTOs.add(new SecretDTO(secret.getId(), secret.getUserId(), contentNode));
         } catch (Exception e) {
             System.out.println("Error parsing decrypted content to JSON: " + e.getMessage());
             // Handle error, maybe add a DTO with an error field
         }
      }

      System.out.println("SecretController.getSecretsByUserId " + secretDTOs);
      return ResponseEntity.ok(secretDTOs);
   }

   // Build Get Secrets by email REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PostMapping("/byemail")
   public ResponseEntity<List<SecretDTO>> getSecretsByEmail(@RequestBody EncryptCredentials credentials) {
      System.out.println("Yay in the controller");
      System.out.println("SecretController.getSecretsByEmail " + credentials);

      User user = userService.findByEmail(credentials.getEmail());
      if (user == null || user.getSalt() == null) {
         System.out.println(
               "SecretController.getSecretsByEmail, user or salt not found for email: " + credentials.getEmail());
         return ResponseEntity.notFound().build();
      }

      List<Secret> secrets = secretService.getSecretsByUserId(user.getId());
      if (secrets.isEmpty()) {
         System.out.println("SecretController.getSecretsByEmail secret isEmpty");
         return ResponseEntity.notFound().build();
      }
      
      ObjectMapper objectMapper = new ObjectMapper();
      List<SecretDTO> secretDTOs = new java.util.ArrayList<>();
      // Decrypt content
      for (Secret secret : secrets) {
         String decryptedContent;
         try {
            // User's salt is already available from the 'user' object fetched above
            decryptedContent = new EncryptUtil(credentials.getEncryptPassword(), user.getSalt(), secretPepper).decrypt(secret.getContent());
         } catch (EncryptionOperationNotPossibleException e) {
            System.out.println("SecretController.getSecretsByEmail " + e + " " + secret);
            decryptedContent = "{\"error\":\"not encryptable. Wrong password?\"}";
         }

          try {
              JsonNode contentNode = objectMapper.readTree(decryptedContent);
              secretDTOs.add(new SecretDTO(secret.getId(), secret.getUserId(), contentNode));
          } catch (Exception e) {
              System.out.println("Error parsing decrypted content to JSON: " + e.getMessage());
              // Optionally handle error, e.g., by creating a DTO with an error message
          }
      }

      System.out.println("SecretController.getSecretsByEmail " + secretDTOs);
      return ResponseEntity.ok(secretDTOs);
   }

   // Build Get All Secrets REST API
   // http://localhost:8080/api/secrets
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @GetMapping
   public ResponseEntity<List<Secret>> getAllSecrets() {
      List<Secret> secrets = secretService.getAllSecrets();
      return new ResponseEntity<>(secrets, HttpStatus.OK);
   }

   // Build Update Secrete REST API
   // http://localhost:8080/api/secrets/1
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @PutMapping("{id}")
   public ResponseEntity<String> updateSecret(
         @PathVariable("id") Long secretId,
         @Valid @RequestBody NewSecret newSecret,
         BindingResult bindingResult) {
      // input validation
      if (bindingResult.hasErrors()) {
         List<String> errors = bindingResult.getFieldErrors().stream()
               .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
               .collect(Collectors.toList());
         System.out.println("SecretController.createSecret " + errors);

         JsonArray arr = new JsonArray();
         errors.forEach(arr::add);
         JsonObject obj = new JsonObject();
         obj.add("message", arr);
         String json = new Gson().toJson(obj);

         System.out.println("SecretController.updateSecret, validation fails: " + json);
         return ResponseEntity.badRequest().body(json);
      }

      // get Secret with id
      Secret dbSecrete = secretService.getSecretById(secretId);
      if (dbSecrete == null) {
         System.out.println("SecretController.updateSecret, secret not found in db");
         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Secret not found in db");
         String json = new Gson().toJson(obj);
         System.out.println("SecretController.updateSecret failed:" + json);
         return ResponseEntity.badRequest().body(json);
      }
      User user = userService.findByEmail(newSecret.getEmail());
      if (user == null || user.getSalt() == null) {
         System.out.println("SecretController.updateSecret, user or salt not found for email: " + newSecret.getEmail());
         JsonObject obj = new JsonObject();
         obj.addProperty("message", "User or salt not found, cannot update secret.");
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Gson().toJson(obj));
      }

      // check if Secret in db has not same userid
      if (dbSecrete.getUserId() != user.getId()) {
         System.out.println("SecretController.updateSecret, not same user id");
         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Secret has not same user id");
         String json = new Gson().toJson(obj);
         System.out.println("SecretController.updateSecret failed:" + json);
         return ResponseEntity.badRequest().body(json);
      }
      // check if Secret can be decrypted with password
      try {
         // Use user's salt for decryption check
         new EncryptUtil(newSecret.getEncryptPassword(), user.getSalt(), secretPepper).decrypt(dbSecrete.getContent());
      } catch (EncryptionOperationNotPossibleException e) {
         System.out.println("SecretController.updateSecret, invalid password");
         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Password not correct.");
         String json = new Gson().toJson(obj);
         System.out.println("SecretController.updateSecret failed:" + json);
         return ResponseEntity.badRequest().body(json);
      }
      // modify Secret in db.
      Secret secret = new Secret(
            secretId,
            user.getId(),
            // Use user's salt for encryption
            new EncryptUtil(newSecret.getEncryptPassword(), user.getSalt(), secretPepper).encrypt(newSecret.getContent().toString()));
      Secret updatedSecret = secretService.updateSecret(secret);
      // save secret in db
      secretService.createSecret(secret);
      System.out.println("SecretController.updateSecret, secret updated in db");
      JsonObject obj = new JsonObject();
      obj.addProperty("answer", "Secret updated");
      String json = new Gson().toJson(obj);
      System.out.println("SecretController.updateSecret " + json);
      return ResponseEntity.accepted().body(json);
   }

   // Build Delete Secret REST API
   @CrossOrigin(origins = "${CROSS_ORIGIN}")
   @DeleteMapping("{id}")
   public ResponseEntity<String> deleteSecret(@PathVariable("id") Long secretId) {
      // todo: Some kind of brute force delete, perhaps test first userid and
      // encryptpassword
      secretService.deleteSecret(secretId);
      System.out.println("SecretController.deleteSecret succesfully: " + secretId);
      return new ResponseEntity<>("Secret successfully deleted!", HttpStatus.OK);
   }
}
