package vn.sun.public_service_manager.controller;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import vn.sun.public_service_manager.dto.UserCreateDTO;
import vn.sun.public_service_manager.dto.UserFilterDTO;
import vn.sun.public_service_manager.dto.UserListDTO;
import vn.sun.public_service_manager.entity.Department;
import vn.sun.public_service_manager.entity.Role;
import vn.sun.public_service_manager.repository.DepartmentRepository;
import vn.sun.public_service_manager.repository.RoleRepository;
import vn.sun.public_service_manager.service.UserManagementService;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserManagementController {

    private final UserManagementService userManagementService;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;

    @GetMapping
    public String listUsers(
            @RequestParam(required = false, defaultValue = "USER") String type,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            Model model) {

        // Sanitize / normalize input
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 100); // prevent excessive page size
        String q = (search == null ? null : search.trim());
        String roleFilter = (role == null || role.isBlank()) ? null : role.trim();

        // Allowlist for sortable fields to avoid SQL errors / injection risk
        List<String> allowedSortFields = List.of("createdAt", "username", "fullName", "email");
        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "createdAt";
        }

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page - 1, size, sort);

        // Build filter DTO
        UserFilterDTO filter = new UserFilterDTO(type, roleFilter, q, active, departmentId);

        // Query service
        Page<UserListDTO> usersPage = userManagementService.getAllUsers(filter, pageable);

        // If requested page is out of range, load last page
        if (usersPage.getTotalPages() > 0 && page > usersPage.getTotalPages()) {
            pageable = PageRequest.of(usersPage.getTotalPages() - 1, size, sort);
            usersPage = userManagementService.getAllUsers(filter, pageable);
            page = usersPage.getTotalPages();
        }

        // Add attributes for view (including useful pagination/sort helpers)
        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalItems", usersPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equalsIgnoreCase("ASC") ? "DESC" : "ASC");
        model.addAttribute("filter", filter);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("roles", roleRepository.findAll());

        return "admin/user_management";
    }

    @GetMapping("/{id}")
    public String getUserDetail(
            @PathVariable Long id,
            @RequestParam String type,
            Model model) {
        UserListDTO user = userManagementService.getUserById(id, type);
        List<Department> departments = departmentRepository.findAll();
        List<Role> roles = roleRepository.findAll();

        model.addAttribute("user", user);
        model.addAttribute("departments", departments);
        model.addAttribute("roles", roles);

        return "admin/user_detail";
    }

    @PostMapping("/create")
    public String createUser(
            @RequestParam String type,
            @ModelAttribute UserCreateDTO dto,
            RedirectAttributes redirectAttributes) {
        try {
            userManagementService.createUser(dto, type);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo người dùng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/update")
    public String updateUser(
            @PathVariable Long id,
            @RequestParam String type,
            @ModelAttribute UserCreateDTO dto,
            RedirectAttributes redirectAttributes) {
        try {
            userManagementService.updateUser(id, dto, type);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật người dùng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(
            @PathVariable Long id,
            @RequestParam String type,
            RedirectAttributes redirectAttributes) {
        try {
            userManagementService.deleteUser(id, type);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa người dùng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleUserActive(
            @PathVariable Long id,
            @RequestParam String type,
            @RequestParam boolean active,
            RedirectAttributes redirectAttributes) {
        try {
            userManagementService.toggleUserActive(id, active, type);
            redirectAttributes.addFlashAttribute("successMessage", "Thay đổi trạng thái thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/export")
    public void exportStaffToCsv(HttpServletResponse response) {
        try {
            response.setContentType("text/csv; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"staff_list_" + System.currentTimeMillis() + ".csv\"");

            Writer writer = new OutputStreamWriter(
                    response.getOutputStream(),
                    StandardCharsets.UTF_8);

            userManagementService.exportStaffToCsv(writer);
            writer.flush();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xuất file CSV", e);
        }
    }

    @PostMapping("/import")
    public String importStaffFromCsv(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "File không được để trống!");
            return "redirect:/admin/users";
        }

        if (!file.getOriginalFilename().endsWith(".csv")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ chấp nhận file CSV!");
            return "redirect:/admin/users";
        }

        try {
            Map<String, Object> result = userManagementService.importStaffFromCsv(file);
            int success = (int) result.get("success");
            int skipped = (int) result.get("skipped");
            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) result.get("errors");

            if (success > 0) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Import thành công " + success + " người dùng" +
                                (skipped > 0 ? ", bỏ qua " + skipped + " ID trùng" : ""));
            }

            if (!errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("importErrors", errors);
                if (success == 0) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Import thất bại!");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }
}
