package vn.sun.public_service_manager.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
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
import vn.sun.public_service_manager.repository.UserRepository;
import vn.sun.public_service_manager.service.ApplicationService;
import vn.sun.public_service_manager.service.ServiceTypeService;
import vn.sun.public_service_manager.utils.constant.StatusEnum;

@Controller
@RequestMapping("/admin/applications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF')")
public class AdminApplicationController {

    private final ApplicationService applicationService;
    private final ServiceTypeService serviceTypeService;
    private final UserRepository userRepository;

    @GetMapping
    public String listApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) StatusEnum status,
            @RequestParam(required = false) Long serviceTypeId,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) String citizenNationalId,
            @RequestParam(required = false) String citizenName,
            @RequestParam(required = false) Long assignedStaffId,
            Model model) {

        // Build filter
        ApplicationFilterDTO filter = new ApplicationFilterDTO();
        filter.setStatus(status);
        filter.setServiceTypeId(serviceTypeId);
        filter.setServiceId(serviceId);
        filter.setCitizenNationalId(citizenNationalId);
        filter.setCitizenName(citizenName);
        filter.setAssignedStaffId(assignedStaffId);

        // Create pageable with sorting by submitted date descending
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));

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
        model.addAttribute("serviceTypes", serviceTypeService.getAllServiceTypes());

        return "admin/application_list";
    }
    @GetMapping("/{id}")
    public String viewApplicationDetail(@PathVariable Long id, Model model) {
        try {
            ApplicationResDTO application = applicationService.getApplicationById(id);
            model.addAttribute("applicationDetail", application);
            model.addAttribute("statuses", StatusEnum.values());
            model.addAttribute("staffList", userRepository.findAllStaff());
            return "admin/application_detail";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error/404";
        }
    }

    @PostMapping("/{id}/update-status")
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
}
