package vn.sun.public_service_manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.sun.public_service_manager.entity.Application;
import vn.sun.public_service_manager.entity.ApplicationDocument;
import vn.sun.public_service_manager.entity.ApplicationStatus;
import vn.sun.public_service_manager.entity.Service;
import vn.sun.public_service_manager.entity.ServiceRequirement;
import vn.sun.public_service_manager.utils.constant.StatusEnum;
import vn.sun.public_service_manager.utils.constant.UploadType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResApiDTO {

    private ApplicationInfo application;
    private List<DocumentInfo> submittedDocuments;
    private List<ProcessingLog> processingLogs;
    private List<AdditionalDocumentRequest> additionalDocumentRequest;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationInfo {
        private Long id;
        private String code;
        private String note;
        private ServiceInfo service;
        private List<String> requirements;
        private LocalDateTime submittedAt;
        private StatusEnum status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInfo {
        private Long id;
        private String name;
        private String description;
        private Integer processingTime;
        private BigDecimal fee;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentInfo {
        private String file;
        private String url;
        private LocalDateTime uploadedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessingLog {
        private StatusEnum status;
        private LocalDateTime updatedAt;
        private String note;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdditionalDocumentRequest {
        private String message;
        private LocalDateTime createdAt;
    }

    /**
     * Convert Application entity to ApplicationResApiDTO
     * 
     * @param application Application entity with all relationships loaded
     * @return ApplicationResApiDTO with full details
     */
    public static ApplicationResApiDTO fromEntity(Application application) {
        if (application == null) {
            return null;
        }

        ApplicationResApiDTO dto = new ApplicationResApiDTO();

        // ========== APPLICATION INFO ==========
        ApplicationInfo appInfo = new ApplicationInfo();
        appInfo.setId(application.getId());
        appInfo.setCode(application.getApplicationCode());
        appInfo.setNote(application.getNote());
        appInfo.setSubmittedAt(application.getSubmittedAt());
        appInfo.setStatus(getLatestStatus(application));

        // Service info
        if (application.getService() != null) {
            Service service = application.getService();

            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setId(service.getId());
            serviceInfo.setName(service.getName());
            serviceInfo.setDescription(service.getDescription());
            serviceInfo.setProcessingTime(service.getProcessingTime());
            serviceInfo.setFee(service.getFee());

            appInfo.setService(serviceInfo);

            // Requirements
            if (service.getServiceRequirements() != null && !service.getServiceRequirements().isEmpty()) {
                List<String> requirements = service.getServiceRequirements().stream()
                        .sorted(Comparator.comparing(ServiceRequirement::isRequired).reversed()) // Required first
                        .map(ServiceRequirement::getName)
                        .collect(Collectors.toList());
                appInfo.setRequirements(requirements);
            } else {
                appInfo.setRequirements(new ArrayList<>());
            }
        }

        dto.setApplication(appInfo);

        // ========== SUBMITTED DOCUMENTS ==========
        List<DocumentInfo> documents = new ArrayList<>();
        if (application.getDocuments() != null && !application.getDocuments().isEmpty()) {
            documents = application.getDocuments().stream()
                    .sorted(Comparator.comparing(ApplicationDocument::getUploadedAt).reversed())
                    .map(doc -> new DocumentInfo(
                            doc.getFileName(),
                            buildDocumentUrl(doc),
                            doc.getUploadedAt()))
                    .collect(Collectors.toList());
        }
        dto.setSubmittedDocuments(documents);

        // ========== PROCESSING LOGS ==========
        List<ProcessingLog> logs = new ArrayList<>();
        if (application.getStatuses() != null && !application.getStatuses().isEmpty()) {
            logs = application.getStatuses().stream()
                    .sorted(Comparator.comparing(ApplicationStatus::getUpdatedAt).reversed())
                    .map(status -> {
                        String note = status.getNote();

                        return new ProcessingLog(
                                status.getStatus(),
                                status.getUpdatedAt(),
                                note);
                    })
                    .collect(Collectors.toList());
        }
        dto.setProcessingLogs(logs);

        // ========== ADDITIONAL DOCUMENT REQUESTS ==========
        List<AdditionalDocumentRequest> requests = new ArrayList<>();
        if (application.getDocuments() != null && !application.getDocuments().isEmpty()) {
            requests = application.getDocuments().stream()
                    .filter(doc -> doc.getType().equals(UploadType.STAFF_FEEDBACK))
                    .map(doc -> {
                        String message = doc.getReason();
                        if (message == null || message.trim().isEmpty()) {
                            message = "Yêu cầu bổ sung tài liệu";
                        }
                        return new AdditionalDocumentRequest(message, doc.getUploadedAt());
                    })
                    .collect(Collectors.toList());
        }
        dto.setAdditionalDocumentRequest(requests);

        return dto;
    }

    private static StatusEnum getLatestStatus(Application application) {
        if (application.getStatuses() == null || application.getStatuses().isEmpty()) {
            return StatusEnum.PROCESSING;
        }

        return application.getStatuses().stream()
                .filter(status -> status.getUpdatedAt() != null)
                .max(Comparator.comparing(ApplicationStatus::getUpdatedAt))
                .map(ApplicationStatus::getStatus)
                .orElse(StatusEnum.PROCESSING);
    }

    /**
     * Build document URL for download/view
     */
    private static String buildDocumentUrl(ApplicationDocument doc) {
        if (doc.getFileName() == null || doc.getFileName().isEmpty()) {
            return "";
        }

        return doc.getFileName();
    }

}