package vn.sun.public_service_manager.dto;

import lombok.Data;
import vn.sun.public_service_manager.entity.Gender;

import java.util.Date;

@Data
public class CitizenRegistrationDto {
    private String nationalId;
    private String fullName;
    private String password; // Mật khẩu thô (chưa mã hóa)
    private Date dob;
    private Gender gender;
    private String address;
    private String phone;
    private String email;
}