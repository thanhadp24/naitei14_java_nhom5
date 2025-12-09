package vn.sun.public_service_manager.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
            @RequestParam(value = "size", defaultValue = "2") int size,
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

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("citizen", citizenService.getById(id));
        return "citizen/citizen-detail";
    }
}
