package vn.sun.public_service_manager.dto;

import lombok.Data;
import vn.sun.public_service_manager.entity.Gender;

import java.util.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class CitizenRegistrationDto {

    @NotBlank(message = "CMND/CCCD không được để trống")
    @Size(min = 9, max = 12, message = "CMND/CCCD phải có độ dài từ 9 đến 12 ký tự")
    private String nationalId;

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password; // Mật khẩu thô (chưa mã hóa)
    private Date dob;

    private Gender gender;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(min = 10, max = 15, message = "Số điện thoại phải có độ dài từ 10 đến 15 ký tự")
    private String phone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
}