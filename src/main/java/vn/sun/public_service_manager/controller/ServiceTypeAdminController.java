package vn.sun.public_service_manager.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import vn.sun.public_service_manager.entity.ServiceType;
import vn.sun.public_service_manager.service.ServiceTypeService;
import vn.sun.public_service_manager.utils.annotation.LogActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/servicetypes")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ServiceTypeAdminController {

    private final ServiceTypeService serviceTypeService;

    @GetMapping
    public String listServiceTypes(Model model) {
        model.addAttribute("serviceTypes", serviceTypeService.getAllServiceTypes());
        return "admin/servicetype_list";
    }

    @GetMapping("/new")
    public String showNewForm(Model model) {
        model.addAttribute("serviceType", new ServiceType());
        model.addAttribute("pageTitle", "Tạo Loại Dịch Vụ Mới");
        return "admin/servicetype_form";
    }

    @LogActivity(action = "Chỉnh Sửa Loại Dịch Vụ", targetType = "SERVICE_TYPE", description = "Chỉnh sửa loại dịch vụ với ID: {id}")
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            ServiceType serviceType = serviceTypeService.getServiceTypeById(id)
                    .orElseThrow(() -> new IllegalArgumentException("ServiceType ID " + id + " not found."));
            model.addAttribute("serviceType", serviceType);
            model.addAttribute("pageTitle", "Chỉnh Sửa Loại Dịch Vụ (ID: " + id + ")");
            return "admin/servicetype_form";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/servicetypes";
        }
    }

    @LogActivity(action = "Lưu Loại Dịch Vụ", targetType = "SERVICE_TYPE", description = "Lưu loại dịch vụ với ID: {serviceType.id}")
    @PostMapping("/save")
    public String saveServiceType(@ModelAttribute("serviceType") ServiceType serviceType, RedirectAttributes ra) {
        boolean isNew = (serviceType.getId() == null);
        serviceTypeService.saveServiceType(serviceType);
        String action = isNew ? "Tạo mới" : "Cập nhật";
        ra.addFlashAttribute("successMessage",
                action + " loại dịch vụ '" + serviceType.getCategory() + "' thành công.");
        return "redirect:/admin/servicetypes";
    }

    @LogActivity(action = "Xóa Loại Dịch Vụ", targetType = "SERVICE_TYPE", description = "Xóa loại dịch vụ với ID: {id}")
    @GetMapping("/delete/{id}")
    public String deleteServiceType(@PathVariable Long id, RedirectAttributes ra) {
        try {
            serviceTypeService.deleteServiceType(id);
            ra.addFlashAttribute("successMessage", "Xóa loại dịch vụ ID " + id + " thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/servicetypes";
    }

}