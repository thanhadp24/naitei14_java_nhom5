package vn.sun.public_service_manager.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import vn.sun.public_service_manager.entity.Service;
import vn.sun.public_service_manager.service.ServiceManagementService;
import vn.sun.public_service_manager.service.ServiceTypeService;
import vn.sun.public_service_manager.utils.annotation.LogActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/services")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF')")
public class ServiceAdminController {

    private final ServiceManagementService serviceManagementService;
    private final ServiceTypeService serviceTypeService;

    @GetMapping
    public String listServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long serviceTypeId,
            Model model) {

        model.addAttribute("servicePage",
                serviceManagementService.getServices(page, size, sortBy, sortDir, keyword, serviceTypeId));
        model.addAttribute("serviceTypes", serviceTypeService.getAllServiceTypes());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", keyword);
        model.addAttribute("serviceTypeId", serviceTypeId);

        return "admin/service_list";
    }

    @GetMapping("/new")
    public String showNewServiceForm(Model model) {
        model.addAttribute("service", new Service());
        model.addAttribute("pageTitle", "Tạo Dịch Vụ Mới");
        model.addAttribute("allTypes", serviceTypeService.getAllServiceTypes());
        // Thêm danh sách Department nếu cần
        return "admin/service_form";
    }

    @GetMapping("/edit/{id}")
    public String showEditServiceForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            Service service = serviceManagementService.getServiceById(id)
                    .orElseThrow(() -> new RuntimeException("Service not found with ID: " + id));

            model.addAttribute("service", service);
            model.addAttribute("pageTitle", "Chỉnh Sửa Dịch Vụ (ID: " + id + ")");
            model.addAttribute("allTypes", serviceTypeService.getAllServiceTypes());

            return "admin/service_form";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/services/list";
        }
    }

    @LogActivity(action = "Quản lý dịch vụ - Tạo/Cập nhật dịch vụ", targetType = "SERVICE")
    @PostMapping("/save")
    public String saveService(
            @ModelAttribute("service") Service service,
            RedirectAttributes redirectAttributes) {
        boolean isNew = (service.getId() == null);
        Service savedService = serviceManagementService.createOrUpdateService(service);
        String action = isNew ? "Tạo mới" : "Cập nhật";
        redirectAttributes.addFlashAttribute("successMessage",
                action + " dịch vụ thành công: " + savedService.getName());

        return "redirect:/admin/services/edit/" + savedService.getId();
    }

    @LogActivity(action = "Quản lý dịch vụ - Xóa dịch vụ", targetType = "SERVICE")
    @GetMapping("/delete/{id}")
    public String deleteService(@PathVariable Long id, RedirectAttributes ra) {
        try {
            serviceManagementService.deleteService(id);
            ra.addFlashAttribute("successMessage", "Xóa dịch vụ ID " + id + " thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể xóa dịch vụ ID " + id + ".");
        }
        return "redirect:/services/list";
    }
    
    @LogActivity(action = "Quản lý dịch vụ - Khôi phục dịch vụ", targetType = "SERVICE")
    @GetMapping("/restore/{id}")
    public String restoreService(@PathVariable Long id, RedirectAttributes ra) {
        try {
            serviceManagementService.restoreService(id);
            ra.addFlashAttribute("successMessage", "Khôi phục dịch vụ ID " + id + " thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Không thể khôi phục dịch vụ ID " + id + ".");
        }
        return "redirect:/services/list";
    }
}