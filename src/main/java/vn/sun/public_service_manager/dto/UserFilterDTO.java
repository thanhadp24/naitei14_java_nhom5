package vn.sun.public_service_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterDTO {
    
    private String type; // "USER", "CITIZEN", "ALL"
    private String role; // "ADMIN", "STAFF", null
    private String search; // Tìm kiếm theo tên, email, phone
    private Boolean active; // true/false/null
    private Long departmentId; // Lọc theo phòng ban
}
