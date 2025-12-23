package vn.sun.public_service_manager.controller;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import vn.sun.public_service_manager.dto.DepartmentDTO;
import vn.sun.public_service_manager.entity.Department;
import vn.sun.public_service_manager.entity.User;
import vn.sun.public_service_manager.repository.DepartmentRepository;
import vn.sun.public_service_manager.repository.UserRespository;
import vn.sun.public_service_manager.service.DepartmentService;

import static java.util.stream.Collectors.toList;

@Controller
@RequestMapping("/admin/departments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDepartmentController {

    private final DepartmentRepository departmentRepository;
    private final UserRespository userRepository;
    private final DepartmentService departmentService;

    @GetMapping
    public String listDepartments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir,
            Model model) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Department> departmentPage = departmentRepository.findAll(pageable);

        model.addAttribute("departments", departmentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", departmentPage.getTotalPages());
        model.addAttribute("totalItems", departmentPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("users", userRepository.findPotentialLeadersForNewDepartment());

        return "admin/department_management";
    }

    @GetMapping("/{id}")
    public String getDepartmentDetail(
            @PathVariable Long id,
            Model model) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng ban không tồn tại"));

        model.addAttribute("department", dept);
        model.addAttribute("users", userRepository.findPotentialLeadersForUpdate(dept.getId()));

        return "admin/department_detail";
    }

    @PostMapping("/create")
    public String createDepartment(
            @ModelAttribute DepartmentDTO dto,
            RedirectAttributes redirectAttributes) {
        try {
            // Check if code exists
            if (departmentRepository.existsByCode(dto.getCode())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mã phòng ban đã tồn tại");
                return "redirect:/admin/departments";
            }

            Department dept = new Department();
            dept.setCode(dto.getCode());
            dept.setName(dto.getName());
            dept.setAddress(dto.getAddress());

            // Set leader if provided
            if (dto.getLeaderId() != null) {
                User leader = userRepository.findById(dto.getLeaderId())
                        .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
                dept.setLeader(leader);
            }

            departmentRepository.save(dept);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo phòng ban thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    @GetMapping(value = "/potential-leaders", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> potentialLeaders(
            @RequestParam(name = "deptId", required = false) Long deptId) {
        List<User> users;
        if (deptId != null) {
            users = userRepository.findPotentialLeadersForUpdate(deptId);
        } else {
            users = userRepository.findPotentialLeadersForNewDepartment();
        }

        return users.stream()
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("username", u.getUsername());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/update")
    public String updateDepartment(
            @PathVariable Long id,
            @ModelAttribute DepartmentDTO dto,
            RedirectAttributes redirectAttributes) {
        try {
            Department dept = departmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Phòng ban không tồn tại"));

            // Check if code exists (exclude current)
            if (!dept.getCode().equals(dto.getCode()) && departmentRepository.existsByCode(dto.getCode())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mã phòng ban đã tồn tại");
                return "redirect:/admin/departments";
            }

            dept.setCode(dto.getCode());
            dept.setName(dto.getName());
            dept.setAddress(dto.getAddress());

            // Update leader
            if (dto.getLeaderId() != null) {
                User leader = userRepository.findById(dto.getLeaderId())
                        .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
                dept.setLeader(leader);
            } else {
                dept.setLeader(null);
            }

            departmentRepository.save(dept);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phòng ban thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    @PostMapping("/{id}/delete")
    public String deleteDepartment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            departmentRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa phòng ban thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    @GetMapping("/export")
    public void exportDepartments(HttpServletResponse response) {
        try {
            response.setContentType("text/csv; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"departments_" + System.currentTimeMillis() + ".csv\"");

            // Write BOM for Excel UTF-8 recognition
            response.getOutputStream().write(0xEF);
            response.getOutputStream().write(0xBB);
            response.getOutputStream().write(0xBF);

            Writer writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
            departmentService.exportDepartmentsToCsv(writer);
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xuất file CSV", e);
        }
    }

    @PostMapping("/import")
    public String importDepartments(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "File không được để trống!");
            return "redirect:/admin/departments";
        }

        if (!file.getOriginalFilename().endsWith(".csv")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chỉ chấp nhận file CSV!");
            return "redirect:/admin/departments";
        }

        try {
            Map<String, Object> result = departmentService.importDepartmentsFromCsv(file);
            int success = (int) result.get("success");
            int skipped = (int) result.get("skipped");
            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) result.get("errors");

            if (success > 0) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Import thành công " + success + " phòng ban" +
                                (skipped > 0 ? ", bỏ qua " + skipped + " mã trùng" : ""));
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

        return "redirect:/admin/departments";
    }
}
