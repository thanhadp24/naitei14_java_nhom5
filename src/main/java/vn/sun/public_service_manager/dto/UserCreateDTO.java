package vn.sun.public_service_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDTO {
    
    // For User
    private String username;
    private String email;
    private String phone;
    private String password;
    private String address;
    private Long departmentId;
    private List<Long> roleIds;
    private Boolean active;
    
    // For Citizen
    private String fullName;
    private String nationalId;
    private String dateOfBirth;
    private String gender;
}
