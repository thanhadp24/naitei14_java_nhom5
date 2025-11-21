package vn.sun.public_service_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.sun.public_service_manager.entity.ServiceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTypeDTO {
    
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer processingTime;
    private BigDecimal fee;
    private Long responsibleDepartmentId;
    private String responsibleDepartmentName;
    private LocalDateTime createdAt;
    
    public static ServiceTypeDTO fromEntity(ServiceType serviceType) {
        ServiceTypeDTO dto = new ServiceTypeDTO();
        dto.setId(serviceType.getId());
        dto.setCode(serviceType.getCode());
        dto.setName(serviceType.getName());
        dto.setDescription(serviceType.getDescription());
        dto.setProcessingTime(serviceType.getProcessingTime());
        dto.setFee(serviceType.getFee());
        dto.setCreatedAt(serviceType.getCreatedAt());
        
        if (serviceType.getResponsibleDepartment() != null) {
            dto.setResponsibleDepartmentId(serviceType.getResponsibleDepartment().getId());
            dto.setResponsibleDepartmentName(serviceType.getResponsibleDepartment().getName());
        }
        
        return dto;
    }
}
