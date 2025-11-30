package vn.sun.public_service_manager.service;

import org.springframework.web.multipart.MultipartFile;

import vn.sun.public_service_manager.dto.response.ApplicationResDTO;
import vn.sun.public_service_manager.entity.Application;

public interface ApplicationService {

    Application createApplication(Long serviceId, String note, MultipartFile[] files);

    ApplicationResDTO getApplicationById(Long id);

    void uploadMoreDocuments(Long applicationId, MultipartFile[] files);
}
