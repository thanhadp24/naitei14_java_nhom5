package vn.sun.public_service_manager.dto;

import lombok.Data;
import vn.sun.public_service_manager.entity.Gender;
import jakarta.validation.constraints.*;
import java.util.Date;

@Data
public class CitizenProfileUpdateRequest {
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100)
    private String fullName;

    private Date dob;
    private Gender gender;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 255)
    private String address;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 15)
    private String phone;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100)
    private String email;
}