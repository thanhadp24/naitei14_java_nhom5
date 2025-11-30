package vn.sun.public_service_manager.dto;

import lombok.Builder;
import lombok.Data;
import vn.sun.public_service_manager.entity.Gender;
import java.util.Date;

@Data
@Builder
public class CitizenProfileResponse {
    private String nationalId;
    private String fullName;
    private Date dob;
    private Gender gender;
    private String address;
    private String phone;
    private String email;
}