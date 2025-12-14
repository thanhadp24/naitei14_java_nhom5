package vn.sun.public_service_manager.dto.request;

import lombok.Data;

@Data
public class AssignStaffDTO {
    private Long applicationId;
    private Long staffId;
    private String note;
}
