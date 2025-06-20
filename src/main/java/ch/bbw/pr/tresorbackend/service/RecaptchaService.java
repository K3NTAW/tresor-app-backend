package ch.bbw.pr.tresorbackend.service;

import ch.bbw.pr.tresorbackend.model.dto.RecaptchaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RecaptchaService {

    private final RestTemplate restTemplate;

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public RecaptchaService() {
        this.restTemplate = new RestTemplate();
    }

    public boolean verifyRecaptcha(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        String url = String.format("%s?secret=%s&response=%s", RECAPTCHA_VERIFY_URL, recaptchaSecret, token);
        
        try {
            RecaptchaResponse response = restTemplate.postForObject(url, null, RecaptchaResponse.class);
            return response != null && response.isSuccess();
        } catch (Exception e) {
            // Log the exception
            return false;
        }
    }
} 