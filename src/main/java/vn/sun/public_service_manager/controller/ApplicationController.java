package vn.sun.public_service_manager.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.sun.public_service_manager.dto.response.ApplicationPageResponse;
import vn.sun.public_service_manager.dto.response.ApplicationResApiDTO;
import vn.sun.public_service_manager.dto.response.ApplicationResDTO;
import vn.sun.public_service_manager.dto.response.FileResDTO;
import vn.sun.public_service_manager.dto.response.MailResDTO;
import vn.sun.public_service_manager.entity.Application;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.exception.FileException;
import vn.sun.public_service_manager.service.ApplicationService;
import vn.sun.public_service_manager.service.CitizenService;
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
    private final CitizenService citizenService;

    public ApplicationController(FileUtil fileUtil, ApplicationService applicationService, EmailService emailService,
            CitizenService citizenService) {
        this.fileUtil = fileUtil;
        this.applicationService = applicationService;
        this.emailService = emailService;
        this.citizenService = citizenService;
    }

    @GetMapping
    @ApiMessage("Get all applications successfully")
    public ResponseEntity<ApplicationPageResponse> getAllApplications(
            @Parameter(description = "Số trang (bắt đầu từ 1)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Số dịch vụ mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo field", example = "id") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Chiều sắp xếp: asc hoặc desc", example = "asc") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Tìm kiếm theo tên dịch vụ hoặc mã", example = "") @RequestParam(required = false) String keyword) {
        String nationalId = SecurityUtil.getCurrentUserName();
        Citizen citizen = citizenService.getByNationalId(nationalId);
        if (page < 1)
            page = 1;
        ApplicationPageResponse response = applicationService.getAllApplicationsForCitizen(
                citizen.getId(), page, size, sortBy, sortDir, keyword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @ApiMessage("Get application by ID successfully")
    public ResponseEntity<ApplicationResApiDTO> getApplicationById(@PathVariable("id") Long id) {
        String nationalId = SecurityUtil.getCurrentUserName();
        Citizen citizen = citizenService.getByNationalId(nationalId);
        return ResponseEntity.ok(applicationService.getApplicationDetail(id, citizen.getId()));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiMessage("Upload application with files successfully")
    @Operation(summary = "Tạo hồ sơ mới với file đính kèm", description = "Upload file và thông tin hồ sơ. File hỗ trợ: pdf, doc, docx, jpg, png")
    public ResponseEntity<FileResDTO> createApplication(
            @Parameter(description = "ID dịch vụ", required = true, example = "1") @RequestParam("serviceId") Long serviceId,
            @Parameter(description = "Ghi chú", required = true, example = "Hồ sơ cấp CMND mới") @RequestParam("note") String note,
            @Parameter(description = "File đính kèm (pdf, doc, docx, jpg, png)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart(value = "files", required = false) MultipartFile[] files)
            throws FileException {

        if (files == null || files.length == 0) {
            throw new FileException("No files uploaded.");
        }

        List<String> allowedExtensions = List.of("pdf", "doc", "docx", "jpg", "png");
        fileUtil.validateFileExtensions(files, allowedExtensions);

        // create user folder if not exists
        String username = SecurityUtil.getCurrentUserName();
        Citizen citizen = citizenService.getByNationalId(username);
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
