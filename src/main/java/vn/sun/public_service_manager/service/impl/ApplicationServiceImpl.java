package vn.sun.public_service_manager.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import vn.sun.public_service_manager.dto.response.ApplicationResDTO;
import vn.sun.public_service_manager.entity.Application;
import vn.sun.public_service_manager.entity.ApplicationDocument;
import vn.sun.public_service_manager.entity.ApplicationStatus;
import vn.sun.public_service_manager.exception.ResourceNotFoundException;
import vn.sun.public_service_manager.repository.ApplicationDocumentRepository;
import vn.sun.public_service_manager.repository.ApplicationRepository;
import vn.sun.public_service_manager.repository.ApplicationStatusRepository;
import vn.sun.public_service_manager.repository.ServiceRepository;
import vn.sun.public_service_manager.service.ApplicationService;
import vn.sun.public_service_manager.utils.constant.StatusEnum;
import vn.sun.public_service_manager.utils.constant.UploadType;

@Service
public class ApplicationServiceImpl implements ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ServiceRepository serviceRepository;
    private final ApplicationStatusRepository applicationStatusRepository;
    private final ApplicationDocumentRepository applicationDocumentRepository;

    public ApplicationServiceImpl(
            ApplicationRepository applicationRepository,
            ServiceRepository serviceRepository,
            ApplicationStatusRepository applicationStatusRepository,
            ApplicationDocumentRepository applicationDocumentRepository) {
        this.applicationRepository = applicationRepository;
        this.serviceRepository = serviceRepository;
        this.applicationStatusRepository = applicationStatusRepository;
        this.applicationDocumentRepository = applicationDocumentRepository;
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
    public ApplicationResDTO getApplicationById(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
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

        if (application.getStatuses() != null && !application.getStatuses().isEmpty()) {
            dto.setStatus(application.getStatuses().get(0).getStatus());
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
        }

        // Map requirements
        if (application.getService().getServiceRequirements() != null) {
            dto.setRequirements(
                    application.getService().getServiceRequirements().stream().map(sr -> sr.getName()).toList());
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
        return dto;
    }
}
