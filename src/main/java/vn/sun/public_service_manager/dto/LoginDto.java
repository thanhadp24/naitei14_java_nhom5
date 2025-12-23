package vn.sun.public_service_manager.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginDto {

    @NotBlank(message = "CMND/CCCD không được để trống")
    @Size(min = 9, max = 12, message = "CMND/CCCD phải có độ dài từ 9 đến 12 ký tự")
    private String nationalId;
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}