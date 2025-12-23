package vn.sun.public_service_manager.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import vn.sun.public_service_manager.dto.ApplicationDTO;
import vn.sun.public_service_manager.dto.ApplicationFilterDTO;
import vn.sun.public_service_manager.dto.request.AssignStaffDTO;
import vn.sun.public_service_manager.dto.request.UpdateApplicationStatusDTO;
import vn.sun.public_service_manager.dto.response.ApplicationPageResponse;
import vn.sun.public_service_manager.dto.response.ApplicationResApiDTO;
import vn.sun.public_service_manager.dto.response.ApplicationResDTO;
import vn.sun.public_service_manager.entity.Application;
import vn.sun.public_service_manager.entity.ApplicationDocument;
import vn.sun.public_service_manager.entity.ApplicationStatus;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.entity.User;
import vn.sun.public_service_manager.exception.ResourceNotFoundException;
import vn.sun.public_service_manager.repository.*;
import vn.sun.public_service_manager.repository.specification.ApplicationSpecification;
import vn.sun.public_service_manager.service.ApplicationService;
import vn.sun.public_service_manager.utils.constant.StatusEnum;
import vn.sun.public_service_manager.utils.constant.UploadType;

import java.time.LocalDateTime;

@Service
public class ApplicationServiceImpl implements ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ServiceRepository serviceRepository;
    private final ApplicationStatusRepository applicationStatusRepository;
    private final ApplicationDocumentRepository applicationDocumentRepository;
    private final CitizenRepository citizenRepository;
    private final UserRepository userRepository;

    public ApplicationServiceImpl(
            ApplicationRepository applicationRepository,
            ServiceRepository serviceRepository,
            ApplicationStatusRepository applicationStatusRepository,
            ApplicationDocumentRepository applicationDocumentRepository,
            CitizenRepository citizenRepository,
            UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.serviceRepository = serviceRepository;
        this.applicationStatusRepository = applicationStatusRepository;
        this.applicationDocumentRepository = applicationDocumentRepository;
        this.citizenRepository = citizenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public Application createApplication(Long serviceId, String note, MultipartFile[] files) {
        vn.sun.public_service_manager.entity.Service serviceInDb = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + serviceId));

        // save application data
        Application application = new Application();
        application.setService(serviceInDb);
        application.setNote(note);
        Application applicationInDb = applicationRepository.save(application);

        // save application status
        ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setApplication(applicationInDb);
        applicationStatus.setStatus(StatusEnum.PROCESSING);
        applicationStatusRepository.save(applicationStatus);

        // save application documents
        for (MultipartFile file : files) {
            ApplicationDocument applicationDocument = new ApplicationDocument();
            applicationDocument.setApplication(applicationInDb);
            applicationDocument.setFileName(file.getOriginalFilename());
            applicationDocument.setType(UploadType.USER_UPLOAD);

            applicationDocumentRepository.save(applicationDocument);
        }
        return applicationInDb;
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResDTO getApplicationById(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Hồ sơ không tồn tại hoặc bạn không có quyền truy cập."));

        // Force load lazy collections within transaction
        if (application.getStatuses() != null) {
            application.getStatuses().size();
        }
        if (application.getDocuments() != null) {
            application.getDocuments().size();
        }
        if (application.getService() != null && application.getService().getServiceRequirements() != null) {
            application.getService().getServiceRequirements().size();
        }

        return mapToDTO(application);
    }

    @Transactional
    @Override
    public void uploadMoreDocuments(Long applicationId, MultipartFile[] files) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));
        // save additional application documents
        for (MultipartFile file : files) {
            ApplicationDocument applicationDocument = new ApplicationDocument();
            applicationDocument.setApplication(application);
            applicationDocument.setFileName(file.getOriginalFilename());
            applicationDocument.setType(UploadType.USER_UPLOAD);

            applicationDocumentRepository.save(applicationDocument);
        }

    }

    private ApplicationResDTO mapToDTO(Application application) {
        ApplicationResDTO dto = new ApplicationResDTO();
        dto.setId(application.getId());
        dto.setCode(application.getApplicationCode());
        dto.setNote(application.getNote());
        dto.setSubmittedAt(application.getSubmittedAt());

        // Get latest status from application_status table, or default to PROCESSING
        if (application.getStatuses() != null && !application.getStatuses().isEmpty()) {
            dto.setStatus(application.getStatuses().get(0).getStatus());
        } else {
            // Default status if no status records exist
            dto.setStatus(StatusEnum.PROCESSING);
        }

        // Map service details
        if (application.getService() != null) {
            ApplicationResDTO.ApplicationService serviceDTO = new ApplicationResDTO.ApplicationService();
            serviceDTO.setId(application.getService().getId());
            serviceDTO.setName(application.getService().getName());
            serviceDTO.setDescription(application.getService().getDescription());
            serviceDTO.setProcessingTime(application.getService().getProcessingTime());
            serviceDTO.setFee(application.getService().getFee());
            dto.setService(serviceDTO);

            // Map requirements
            if (application.getService().getServiceRequirements() != null) {
                dto.setRequirements(
                        application.getService().getServiceRequirements().stream().map(sr -> sr.getName()).toList());
            }
        }

        // Map documents
        if (application.getDocuments() != null) {
            dto.setDocuments(application.getDocuments().stream().map(doc -> {
                ApplicationResDTO.ApplicationDocument docDTO = new ApplicationResDTO.ApplicationDocument();
                docDTO.setFileName(doc.getFileName());
                docDTO.setUploadedAt(doc.getUploadedAt());
                return docDTO;
            }).toList());
        }

        // Map assigned staff
        if (application.getAssignedStaff() != null) {
            ApplicationResDTO.ApplicationStaff staffDTO = new ApplicationResDTO.ApplicationStaff();
            staffDTO.setId(application.getAssignedStaff().getId());
            staffDTO.setUsername(application.getAssignedStaff().getUsername());
            staffDTO.setEmail(application.getAssignedStaff().getEmail());
            dto.setAssignedStaff(staffDTO);
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationDTO> getApplicationsByCitizen(String nationalId, Pageable pageable) {

        // 1. Tìm Entity Citizen để lấy ID (citizen_id)
        Citizen citizen = citizenRepository.findByNationalId(nationalId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen", "National ID", nationalId));

        Long citizenId = citizen.getId();

        // 2. Truy vấn cơ sở dữ liệu với phân trang và lọc theo citizenId (fetch
        // statuses)
        Page<Application> applicationPage = applicationRepository.findByCitizenIdWithStatuses(citizenId, pageable);

        // 3. Chuyển đổi Page<Application> sang Page<ApplicationDTO>
        return applicationPage.map(ApplicationDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationDTO> getAllApplications(ApplicationFilterDTO filter, Pageable pageable) {
        Specification<Application> spec = ApplicationSpecification.filterApplications(filter);
        Page<Application> applicationPage = applicationRepository.findAll(spec, pageable);
        return applicationPage.map(ApplicationDTO::fromEntity);
    }

    @Override
    @Transactional
    public void updateApplicationStatus(UpdateApplicationStatusDTO dto) {
        Application application = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found with id: " + dto.getApplicationId()));

        // Create new status record
        ApplicationStatus status = new ApplicationStatus();
        status.setApplication(application);
        status.setStatus(dto.getStatus());
        status.setNote(dto.getNote());
        status.setUpdatedAt(LocalDateTime.now());
        // TODO: Set updatedBy from current user

        applicationStatusRepository.save(status);

        // Upload response documents if any
        if (dto.getDocuments() != null && dto.getDocuments().length > 0) {
            for (MultipartFile file : dto.getDocuments()) {
                if (!file.isEmpty()) {
                    ApplicationDocument document = new ApplicationDocument();
                    document.setApplication(application);
                    document.setFileName(file.getOriginalFilename());
                    document.setType(UploadType.STAFF_FEEDBACK);
                    document.setUploadedAt(LocalDateTime.now());
                    applicationDocumentRepository.save(document);
                }
            }
        }
    }

    @Override
    @Transactional
    public void assignStaffToApplication(AssignStaffDTO dto) {
        Application application = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found with id: " + dto.getApplicationId()));

        User staff = userRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getStaffId()));

        application.setAssignedStaff(staff);
        applicationRepository.save(application);

        // Create status log for assignment
        if (dto.getNote() != null && !dto.getNote().isEmpty()) {
            ApplicationStatus status = new ApplicationStatus();
            status.setApplication(application);
            status.setStatus(application.getStatuses().isEmpty() ? StatusEnum.PROCESSING
                    : application.getStatuses().get(0).getStatus());
            status.setNote(dto.getNote());
            status.setUpdatedAt(LocalDateTime.now());
            // TODO: Set updatedBy from current user
            applicationStatusRepository.save(status);
        }
    }

    @Override
    public ApplicationPageResponse getAllApplicationsForCitizen(Long citizenId, int page, int size, String sortBy,
            String sortDir, String keyword) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Application> applicationPage = null;
        if (keyword != null && !keyword.isEmpty()) {
            applicationPage = applicationRepository.findByKeyword(citizenId, keyword, pageable);
        } else {
            applicationPage = applicationRepository.findByCitizenIdWithStatuses(citizenId, pageable);
        }
        return ApplicationPageResponse.fromEntity(applicationPage);
    }

    @Override
    public ApplicationResApiDTO getApplicationDetail(Long id, Long citizenId) {
        Application application = applicationRepository.findByIdWithDetails(id, citizenId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Hồ sơ không tồn tại hoặc bạn không có quyền truy cập."));
        return ApplicationResApiDTO.fromEntity(application);
    }
}
