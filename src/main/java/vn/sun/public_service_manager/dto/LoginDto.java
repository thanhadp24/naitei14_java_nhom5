package vn.sun.public_service_manager.dto;
import lombok.Data;

@Data
public class LoginDto {
    private String nationalId;
    private String password;
}