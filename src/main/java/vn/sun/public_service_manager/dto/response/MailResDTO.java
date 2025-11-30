package vn.sun.public_service_manager.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MailResDTO {
    private String applicationCode;
    private String serviceName;
    private LocalDateTime submittedAt;

}
