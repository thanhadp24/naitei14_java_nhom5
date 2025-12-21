package vn.sun.public_service_manager.dto.response;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.sun.public_service_manager.entity.Application;
import vn.sun.public_service_manager.entity.ApplicationStatus;
import vn.sun.public_service_manager.utils.constant.StatusEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationPageResponse {
    private List<ApplicationDto> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationDto {
        private Long id;
        private String code;
        private LocalDateTime submittedAt;
        private StatusEnum status;
        private ServiceDto service;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceDto {
        private Long id;
        private String name;
        private String code;
    }

    public static ApplicationPageResponse fromEntity(Page<Application> applicationPage) {
        List<ApplicationDto> applicationDtos = applicationPage.getContent().stream()
                .map(app -> {
                    try {
                        ServiceDto serviceDto = null;
                        if (app.getService() != null) {
                            serviceDto = new ServiceDto(
                                    app.getService().getId(),
                                    app.getService().getName(),
                                    app.getService().getCode());
                        }

                        StatusEnum latestStatus = StatusEnum.PROCESSING;
                        if (app.getStatuses() != null && !app.getStatuses().isEmpty()) {
                            latestStatus = app.getStatuses().stream()
                                    .filter(status -> status.getUpdatedAt() != null)
                                    .max(Comparator.comparing(ApplicationStatus::getUpdatedAt))
                                    .map(ApplicationStatus::getStatus)
                                    .orElse(StatusEnum.PROCESSING);
                        }

                        return new ApplicationDto(
                                app.getId(),
                                app.getApplicationCode(),
                                app.getSubmittedAt(),
                                latestStatus,
                                serviceDto);
                    } catch (Exception e) {
                        System.err.println("Error mapping application " + app.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                        return new ApplicationDto(
                                app.getId(),
                                app.getApplicationCode(),
                                app.getSubmittedAt(),
                                StatusEnum.PROCESSING,
                                null);
                    }
                })
                .collect(Collectors.toList());

        return new ApplicationPageResponse(
                applicationDtos,
                applicationPage.getNumber() + 1,
                applicationPage.getTotalPages(),
                applicationPage.getTotalElements(),
                applicationPage.getSize(),
                applicationPage.hasNext(),
                applicationPage.hasPrevious());
    }
}