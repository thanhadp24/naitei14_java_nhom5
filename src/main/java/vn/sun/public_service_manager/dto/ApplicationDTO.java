package vn.sun.public_service_manager.dto;

import java.time.LocalDateTime;
import lombok.Data;
import vn.sun.public_service_manager.entity.Application;
import vn.sun.public_service_manager.utils.constant.StatusEnum;

@Data
public class ApplicationDTO {
    private Long id;
    private String applicationCode;
    private String note;
    private String serviceName;
    private String serviceType;
    private String citizenIdNumber;
    private String citizenName;
    private LocalDateTime submittedAt;
    private String assignedStaffName;
    private StatusEnum status;

    public static ApplicationDTO fromEntity(Application application) {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setId(application.getId());
        dto.setApplicationCode(application.getApplicationCode());
        dto.setNote(application.getNote());

        if (application.getService() != null) {
            dto.setServiceName(application.getService().getName());
            if (application.getService().getServiceType() != null) {
                dto.setServiceType(application.getService().getServiceType().getCategory());
            }
        }

        if (application.getCitizen() != null) {
            dto.setCitizenIdNumber(application.getCitizen().getNationalId());
            dto.setCitizenName(application.getCitizen().getFullName());
        }

        if (application.getAssignedStaff() != null) {
            dto.setAssignedStaffName(application.getAssignedStaff().getUsername());
        }

        // Get latest status (sorted by updatedAt DESC in entity)
        if (application.getStatuses() != null && !application.getStatuses().isEmpty()) {
            dto.setStatus(application.getStatuses().get(0).getStatus());
        }

        dto.setSubmittedAt(application.getSubmittedAt());
        return dto;
    }
}