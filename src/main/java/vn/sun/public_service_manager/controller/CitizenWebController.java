package vn.sun.public_service_manager.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.service.CitizenService;

@Controller
@RequestMapping("/admin/citizens")
public class CitizenWebController {

    private final CitizenService citizenService;

    public CitizenWebController(CitizenService citizenService) {
        this.citizenService = citizenService;
    }

    @GetMapping
    public String index(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
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

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            citizenService.deleteById(id);
            ra.addFlashAttribute("message", "Xóa công dân thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/citizens";
    }

}
