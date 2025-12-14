package vn.sun.public_service_manager.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import vn.sun.public_service_manager.dto.ApplicationDTO;
import vn.sun.public_service_manager.dto.ApplicationFilterDTO;
import vn.sun.public_service_manager.dto.request.AssignStaffDTO;
import vn.sun.public_service_manager.dto.request.UpdateApplicationStatusDTO;
import vn.sun.public_service_manager.dto.response.ApplicationResDTO;
import vn.sun.public_service_manager.entity.Application;
import vn.sun.public_service_manager.utils.constant.StatusEnum;

public interface ApplicationService {

    Application createApplication(Long serviceId, String note, MultipartFile[] files);

    ApplicationResDTO getApplicationById(Long id);

    void uploadMoreDocuments(Long applicationId, MultipartFile[] files);

    Page<ApplicationDTO> getApplicationsByCitizen(String nationalId, Pageable pageable);
    
    // Admin methods
    Page<ApplicationDTO> getAllApplications(ApplicationFilterDTO filter, Pageable pageable);
    
    void updateApplicationStatus(UpdateApplicationStatusDTO dto);
    
    void assignStaffToApplication(AssignStaffDTO dto);
}
