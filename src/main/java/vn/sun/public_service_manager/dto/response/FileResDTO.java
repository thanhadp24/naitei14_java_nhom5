package vn.sun.public_service_manager.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FileResDTO {
    private String applicationId;
    private LocalDateTime uploadedAt;
}
