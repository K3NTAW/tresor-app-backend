package ch.bbw.pr.tresorbackend.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecretDTO {
    private Long id;
    private Long userId;
    private JsonNode content;
} 