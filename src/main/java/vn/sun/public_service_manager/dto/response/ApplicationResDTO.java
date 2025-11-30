package vn.sun.public_service_manager.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.sun.public_service_manager.utils.constant.StatusEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationResDTO {
    private Long id;
    private String code;
    @Column(columnDefinition = "TEXT")
    private String note;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;
    private StatusEnum status;

    private ApplicationService service;
    private List<String> requirements;
    private List<ApplicationDocument> documents;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApplicationService {
        private Long id;
        private String name;
        private String description;
        private int processingTime;
        private BigDecimal fee;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApplicationDocument {
        private String fileName;
        private LocalDateTime uploadedAt;
    }

}
