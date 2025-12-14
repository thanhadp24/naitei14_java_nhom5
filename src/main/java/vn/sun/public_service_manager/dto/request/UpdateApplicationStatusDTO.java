package vn.sun.public_service_manager.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import vn.sun.public_service_manager.utils.constant.StatusEnum;

@Data
public class UpdateApplicationStatusDTO {
    private Long applicationId;
    private StatusEnum status;
    private String note;
    private MultipartFile[] documents;
}
