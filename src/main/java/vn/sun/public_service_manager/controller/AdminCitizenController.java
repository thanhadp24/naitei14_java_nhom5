package vn.sun.public_service_manager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.service.CitizenService;
import vn.sun.public_service_manager.service.UserManagementService;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
@RequestMapping("/admin/citizens")
@RequiredArgsConstructor
public class AdminCitizenController {

    private final CitizenService citizenService;
    private final UserManagementService userManagementService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String index(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {

        if (page < 1)
            page = 1;
        Page<Citizen> pageable = citizenService.getAll(page, size, keyword);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageable.getTotalPages());
        model.addAttribute("totalItems", pageable.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("citizens", pageable.getContent());
        return "citizen/citizens";
    }

    @GetMapping("/new")
    public String create(Model model) {
        if (!model.containsAttribute("citizen")) {
            model.addAttribute("citizen", new Citizen());
        }
        return "citizen/citizen_form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Citizen citizen,
            BindingResult result,
            Model model,
            RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("citizen", citizen);
            return "citizen/citizen_form";
        }

        try {
            boolean isNew = citizen.getId() == null;
            if(isNew) {
                citizen.setPassword(passwordEncoder.encode(citizen.getPassword()));
            }

            citizenService.save(citizen);
            ra.addFlashAttribute("message",
                    isNew ? "Thêm công dân thành công!" : "Cập nhật công dân thành công!");

            return "redirect:/admin/citizens";
        } catch (Exception e) {
            ra.addFlashAttribute("citizen", citizen);
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/citizens/new";
        }
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("citizen", citizenService.getById(id));
        return "citizen/citizen_detail:: modal-content";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("citizen", citizenService.getById(id));

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/citizens";
        }
        return "citizen/citizen_form";
    }

    @GetMapping("/{id}/toggle")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            citizenService.toggleById(id);
            ra.addFlashAttribute("message", "Xóa công dân thành công!");
            ra.addFlashAttribute("message", "Cập nhật trạng thái công dân thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/citizens";
    }

    @GetMapping("/export")
    public void exportCitizens(HttpServletResponse response) {
        try {
            response.setContentType("text/csv; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"citizens_" + System.currentTimeMillis() + ".csv\"");

            Writer writer = new OutputStreamWriter(
                    response.getOutputStream(),
                    StandardCharsets.UTF_8);

            userManagementService.exportCitizensToCsv(writer);

            writer.flush();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xuất file CSV", e);
        }
    }

    @PostMapping("/import")
    public String importCitizens(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes ra) {

        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "File không được để trống!");
            return "redirect:/admin/citizens";
        }

        if (!file.getOriginalFilename().endsWith(".csv")) {
            ra.addFlashAttribute("error", "Chỉ chấp nhận file CSV!");
            return "redirect:/admin/citizens";
        }

        try {
            Map<String, Object> result = userManagementService.importCitizensFromCsv(file);

            int total = (int) result.get("total");
            int success = (int) result.get("success");
            int failed = (int) result.get("failed");

            if (failed > 0) {
                ra.addFlashAttribute("message",
                        String.format("Import hoàn tất: %d/%d thành công, %d thất bại",
                                success, total, failed));
            } else {
                ra.addFlashAttribute("message",
                        String.format("Import thành công %d công dân!", success));
            }

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi import: " + e.getMessage());
        }

        return "redirect:/admin/citizens";
    }

}
