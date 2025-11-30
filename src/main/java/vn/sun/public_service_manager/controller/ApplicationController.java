package vn.sun.public_service_manager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.sun.public_service_manager.dto.response.ApplicationResDTO;
import vn.sun.public_service_manager.dto.response.FileResDTO;
import vn.sun.public_service_manager.dto.response.MailResDTO;
import vn.sun.public_service_manager.entity.Application;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.exception.FileException;
import vn.sun.public_service_manager.repository.CitizenRepository;
import vn.sun.public_service_manager.service.ApplicationService;
import vn.sun.public_service_manager.service.EmailService;
import vn.sun.public_service_manager.utils.FileUtil;
import vn.sun.public_service_manager.utils.SecurityUtil;
import vn.sun.public_service_manager.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final FileUtil fileUtil;
    private final ApplicationService applicationService;
    private final EmailService emailService;
    private final CitizenRepository citizenRepository;

    public ApplicationController(FileUtil fileUtil, ApplicationService applicationService, EmailService emailService,
            CitizenRepository citizenRepository) {
        this.fileUtil = fileUtil;
        this.applicationService = applicationService;
        this.emailService = emailService;
        this.citizenRepository = citizenRepository;
    }

    @GetMapping("/{id}")
    @ApiMessage("Get application by ID successfully")
    public ResponseEntity<ApplicationResDTO> getApplicationById(@PathVariable("id") Long id) {

        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @PostMapping("/upload")
    @ApiMessage("Upload application with files successfully")
    public ResponseEntity<FileResDTO> createApplication(
            @RequestParam("serviceId") Long serviceId,
            @RequestParam("note") String note,
            @RequestParam(value = "files", required = false) MultipartFile[] files) throws FileException {

        if (files == null || files.length == 0) {
            throw new FileException("No files uploaded.");
        }

        List<String> allowedExtensions = List.of("pdf", "doc", "docx", "jpg", "png");
        fileUtil.validateFileExtensions(files, allowedExtensions);

        // create user folder if not exists
        String username = SecurityUtil.getCurrentUserName();
        Citizen citizen = citizenRepository.findByNationalId(username)
                .orElseThrow(() -> new FileException("Citizen with national ID " + username + " not found."));
        fileUtil.createDirectoryIfNotExists(username);

        // save files to user folder
        fileUtil.saveFiles(files, username);

        // save application data
        Application application = applicationService.createApplication(serviceId, note, files);

        MailResDTO mailResDTO = new MailResDTO();
        mailResDTO.setApplicationCode(application.getApplicationCode());
        mailResDTO.setServiceName(application.getService().getName());
        mailResDTO.setSubmittedAt(application.getSubmittedAt());
        // send mail
        emailService.sendApplicationConfirmationEmail(
                citizen.getEmail(),
                citizen.getFullName(), "Hồ sơ đã đăng ký...",
                mailResDTO,
                "email_template");

        FileResDTO response = new FileResDTO();
        response.setApplicationId(application.getApplicationCode());
        response.setUploadedAt(application.getSubmittedAt());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-more")
    @ApiMessage("Upload more files to existing application successfully")
    public ResponseEntity<FileResDTO> uploadMoreFiles(
            @RequestParam("applicationId") Long applicationId,
            @RequestParam(value = "files", required = false) MultipartFile[] files) throws FileException {

        if (files == null || files.length == 0) {
            throw new FileException("No files uploaded.");
        }

        List<String> allowedExtensions = List.of("pdf", "doc", "docx", "jpg", "png");
        fileUtil.validateFileExtensions(files, allowedExtensions);

        // create user folder if not exists
        String username = SecurityUtil.getCurrentUserName();
        fileUtil.createDirectoryIfNotExists(username);

        // save files to user folder
        fileUtil.saveFiles(files, username);

        // save application data
        applicationService.uploadMoreDocuments(applicationId, files);

        FileResDTO response = new FileResDTO();
        response.setApplicationId("Application ID: " + applicationId);
        response.setUploadedAt(java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

}
