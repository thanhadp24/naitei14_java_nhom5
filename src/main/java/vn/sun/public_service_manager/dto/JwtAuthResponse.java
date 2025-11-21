package vn.sun.public_service_manager.dto;
import lombok.Data;

@Data
public class JwtAuthResponse {
    private String token;
    private String tokenType = "Bearer";

    public JwtAuthResponse(String token) {
        this.token = token;
    }
}