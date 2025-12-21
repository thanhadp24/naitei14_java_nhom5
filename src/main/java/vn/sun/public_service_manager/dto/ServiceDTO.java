package vn.sun.public_service_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.sun.public_service_manager.entity.Service;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer processingTime;
    private BigDecimal fee;
    private ServiceTypeService serviceType;
    private DepartmentService department;
    private List<ServiceRequirement> requirements;

    public static ServiceDTO fromEntity(Service service) {
        ServiceDTO dto = new ServiceDTO();
        dto.setId(service.getId());
        dto.setCode(service.getCode());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setProcessingTime(service.getProcessingTime());
        dto.setFee(service.getFee());

        if (service.getResponsibleDepartment() != null) {
            DepartmentService deptDto = new DepartmentService();
            deptDto.setId(service.getResponsibleDepartment().getId());
            deptDto.setName(service.getResponsibleDepartment().getName());
            dto.setDepartment(deptDto);
        }

        if (service.getServiceType() != null) {
            ServiceTypeService typeDto = new ServiceTypeService();
            typeDto.setId(service.getServiceType().getId());
            typeDto.setCategory(service.getServiceType().getCategory());
            dto.setServiceType(typeDto);
        }

        if (service.getServiceRequirements() != null) {
            List<ServiceRequirement> reqDtos = service.getServiceRequirements().stream().map(req -> {
                ServiceRequirement reqDto = new ServiceRequirement();
                reqDto.setName(req.getName());
                reqDto.setDescription(req.getDescription());
                return reqDto;
            }).toList();
            dto.setRequirements(reqDtos);
        }

        return dto;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentService {
        private Long id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceTypeService {
        private Long id;
        private String category;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceRequirement {
        private String name;
        private String description;
    }
}
