package vn.sun.public_service_manager.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import vn.sun.public_service_manager.dto.ApplicationDTO;
import vn.sun.public_service_manager.dto.ApplicationFilterDTO;
import vn.sun.public_service_manager.dto.request.AssignStaffDTO;
import vn.sun.public_service_manager.dto.request.UpdateApplicationStatusDTO;
import vn.sun.public_service_manager.dto.response.ApplicationResDTO;
import vn.sun.public_service_manager.entity.Role;
import vn.sun.public_service_manager.entity.User;
import vn.sun.public_service_manager.repository.RoleRepository;
import vn.sun.public_service_manager.repository.UserRepository;
import vn.sun.public_service_manager.service.ApplicationService;
import vn.sun.public_service_manager.service.ServiceTypeService;
import vn.sun.public_service_manager.utils.annotation.LogActivity;
import vn.sun.public_service_manager.utils.constant.StatusEnum;

@Controller
@RequestMapping("/admin/applications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF')")
public class AdminApplicationController {

    private final ApplicationService applicationService;
    private final ServiceTypeService serviceTypeService;
    private final UserRepository userRepository;
    private final vn.sun.public_service_manager.repository.ApplicationDocumentRepository documentRepository;
    private final vn.sun.public_service_manager.repository.ServiceRepository serviceRepository;
    private final RoleRepository roleRepository;

    @GetMapping
    public String listApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) StatusEnum status,
            @RequestParam(required = false) Long serviceTypeId,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) String citizenNationalId,
            @RequestParam(required = false) String citizenName,
            @RequestParam(required = false) Long assignedStaffId,
            Authentication authentication,
            Model model) {

        // Build filter
        ApplicationFilterDTO filter = new ApplicationFilterDTO();
        filter.setStatus(status);
        filter.setServiceTypeId(serviceTypeId);
        filter.setServiceId(serviceId);
        filter.setCitizenNationalId(citizenNationalId);
        filter.setCitizenName(citizenName);
        filter.setAssignedStaffId(assignedStaffId);

        // Lấy thông tin user hiện tại
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check role và apply filter tương ứng
        boolean isStaff = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_STAFF"));

        boolean isManager = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_MANAGER"));

        if (isStaff) {
            // Nếu là STAFF, chỉ lấy applications được gán cho staff đó
            filter.setAssignedStaffId(currentUser.getId());
            model.addAttribute("isStaff", true);
            model.addAttribute("isManager", false);
        } else if (isManager) {
            // Nếu là MANAGER, chỉ lấy applications của department mình
            if (currentUser.getDepartment() != null) {
                filter.setDepartmentId(currentUser.getDepartment().getId());
            }
            model.addAttribute("isStaff", false);
            model.addAttribute("isManager", true);
        } else {
            // ADMIN xem hết
            model.addAttribute("isStaff", false);
            model.addAttribute("isManager", false);
        }

        // Create pageable with sorting by submitted date descending
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "submittedAt"));

        // Get applications
        Page<ApplicationDTO> applicationPage = applicationService.getAllApplications(filter, pageable);

        // Add to model
        model.addAttribute("applications", applicationPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", applicationPage.getTotalPages());
        model.addAttribute("totalItems", applicationPage.getTotalElements());
        model.addAttribute("size", size);

        // Add filter values to keep in form
        model.addAttribute("filter", filter);
        model.addAttribute("statuses", StatusEnum.values());

        // Load services theo department nếu là MANAGER hoặc STAFF
        List<vn.sun.public_service_manager.entity.Service> services;
        if ((isManager || isStaff) && currentUser.getDepartment() != null) {
            services = serviceRepository
                    .findByResponsibleDepartmentId(currentUser.getDepartment().getId(), Pageable.unpaged())
                    .getContent();
        } else {
            services = serviceRepository.findAll();
        }
        model.addAttribute("services", services);

        // Also load serviceTypes for compatibility
        List<vn.sun.public_service_manager.entity.ServiceType> serviceTypes;
        if ((isManager || isStaff) && currentUser.getDepartment() != null) {
            serviceTypes = serviceTypeService.getServiceTypesByDepartmentId(currentUser.getDepartment().getId());
        } else {
            serviceTypes = serviceTypeService.getAllServiceTypes();
        }
        model.addAttribute("serviceTypes", serviceTypes);

        return "admin/application_list";
    }

    @GetMapping("/{id}")
    public String viewApplicationDetail(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            ApplicationResDTO application = applicationService.getApplicationById(id);
            model.addAttribute("applicationDetail", application);
            model.addAttribute("statuses", StatusEnum.values());

            // Lấy danh sách staff theo department chịu trách nhiệm cho service của
            // application
            List<User> staffList = new ArrayList<>();

            // Lấy service từ application
            var service = serviceRepository.findById(application.getService().getId())
                    .orElseThrow(() -> new RuntimeException("Service not found"));

            Role staffRole = roleRepository.findByName("ROLE_STAFF").orElse(null);
            // Nếu service có responsible department, lấy staff của department đó
            if (service.getResponsibleDepartment() != null) {
                staffList = userRepository.findByDepartmentAndRoles(service.getResponsibleDepartment(),
                        Collections.singletonList(staffRole));

            } else {
                // Nếu service không có department chịu trách nhiệm, không hiện staff nào
                staffList = List.of();
            }

            model.addAttribute("staffList", staffList);
            return "admin/application_detail";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error/404";
        }
    }

    @LogActivity(action = "Update Application Status", targetType = "APPLICATION", description = "Cập nhật trạng thái hồ sơ")
    @PostMapping("/{id}/update-status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF')")
    public String updateStatus(@PathVariable Long id,
            @ModelAttribute UpdateApplicationStatusDTO dto,
            RedirectAttributes redirectAttributes) {
        try {
            dto.setApplicationId(id);
            applicationService.updateApplicationStatus(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/applications/" + id;
    }

    @LogActivity(action = "Assign Staff to Application", targetType = "APPLICATION", description = "Gán nhân viên xử lý hồ sơ")
    @PostMapping("/{id}/assign-staff")
    public String assignStaff(@PathVariable Long id,
            @ModelAttribute AssignStaffDTO dto,
            RedirectAttributes redirectAttributes) {
        try {
            dto.setApplicationId(id);
            applicationService.assignStaffToApplication(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Gán nhân viên thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/applications/" + id;
    }

    @GetMapping("/documents/{documentId}/download")
    public void downloadDocument(
            @PathVariable Long documentId,
            jakarta.servlet.http.HttpServletResponse response) throws Exception {

        vn.sun.public_service_manager.entity.ApplicationDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));

        String citizenNationalId = document.getApplication().getCitizen().getNationalId();
        String fileName = document.getFileName();

        // Build file path
        String uploadDir = "applications";
        java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir, citizenNationalId, fileName);

        if (!java.nio.file.Files.exists(filePath)) {
            response.sendError(jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND,
                    "File not found: " + filePath.toAbsolutePath());
            return;
        }

        // Set response headers
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setContentLengthLong(java.nio.file.Files.size(filePath));

        // Write file to response
        try (java.io.InputStream inputStream = java.nio.file.Files.newInputStream(filePath);
                java.io.OutputStream outputStream = response.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF')")
    public void exportApplications(
            jakarta.servlet.http.HttpServletResponse response,
            org.springframework.security.core.Authentication authentication) {
        try {
            response.setContentType("text/csv; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"application_list_" + System.currentTimeMillis() + ".csv\"");

            // Write BOM for Excel UTF-8 recognition
            response.getOutputStream().write(0xEF);
            response.getOutputStream().write(0xBB);
            response.getOutputStream().write(0xBF);

            java.io.Writer writer = new java.io.OutputStreamWriter(
                    response.getOutputStream(),
                    java.nio.charset.StandardCharsets.UTF_8);

            // Get current user and role
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean isStaff = authentication.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_STAFF"));

            boolean isManager = authentication.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_MANAGER"));

            ApplicationFilterDTO filter = new ApplicationFilterDTO();

            if (isStaff) {
                // STAFF: only export their assigned applications
                filter.setAssignedStaffId(currentUser.getId());
                applicationService.exportApplicationsToCsv(writer, filter);
            } else if (isManager) {
                // MANAGER: only export applications of their department
                if (currentUser.getDepartment() != null) {
                    filter.setDepartmentId(currentUser.getDepartment().getId());
                    applicationService.exportApplicationsToCsv(writer, filter);
                } else {
                    applicationService.exportApplicationsToCsv(writer);
                }
            } else {
                // ADMIN: export all applications
                applicationService.exportApplicationsToCsv(writer);
            }

            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xuất file CSV Hồ sơ", e);
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public String deleteApplication(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            applicationService.softDeleteApplication(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa hồ sơ thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/applications";
    }
}
