package vn.sun.public_service_manager.dto;

import lombok.Data;
import vn.sun.public_service_manager.utils.constant.StatusEnum;

@Data
public class ApplicationFilterDTO {
    private StatusEnum status;
    private Long serviceTypeId;
    private Long serviceId;
    private String citizenNationalId;
    private String citizenName;
    private Long assignedStaffId;
}
