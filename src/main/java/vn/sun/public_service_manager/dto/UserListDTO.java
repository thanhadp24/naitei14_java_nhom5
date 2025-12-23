package vn.sun.public_service_manager.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListDTO {
    
    private Long id;
    private String type; // "USER" hoặc "CITIZEN"
    
    // Thông tin chung
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
    
    // Riêng cho User
    private String username;
    private String departmentName;
    private Long departmentId;
    private Set<String> roles;
    private Set<Long> roleIds;
    private Boolean active;
    
    // Riêng cho Citizen
    private String nationalId;
    private Date dateOfBirth;
    private String gender;
}
