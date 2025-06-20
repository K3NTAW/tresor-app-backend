package ch.bbw.pr.tresorbackend.model;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
} 