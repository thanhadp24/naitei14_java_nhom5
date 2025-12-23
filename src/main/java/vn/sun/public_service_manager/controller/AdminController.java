package vn.sun.public_service_manager.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.sun.public_service_manager.entity.ActivityLog;
import vn.sun.public_service_manager.repository.ActivityLogRepository;
import vn.sun.public_service_manager.repository.ApplicationRepository;
import vn.sun.public_service_manager.repository.ApplicationStatusRepository;
import vn.sun.public_service_manager.repository.UserRepository;
import vn.sun.public_service_manager.utils.constant.StatusEnum;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusRepository applicationStatusRepository;
    private final ActivityLogRepository activityLogRepository;

    public AdminController(UserRepository userRepository,
            ApplicationRepository applicationRepository,
            ApplicationStatusRepository applicationStatusRepository,
            ActivityLogRepository activityLogRepository) {
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.applicationStatusRepository = applicationStatusRepository;
        this.activityLogRepository = activityLogRepository;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
        }
        if (logout != null) {
            model.addAttribute("message", "Đăng xuất thành công!");
        }
        return "admin/login";
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_STAFF')")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("roles", authentication.getAuthorities());

        // Load real statistics from database
        long totalUsers = userRepository.count();
        long totalApplications = applicationRepository.count();

        // Count applications by latest status
        long processingCount = applicationStatusRepository.countByLatestStatus(StatusEnum.PROCESSING);
        long approvedCount = applicationStatusRepository.countByLatestStatus(StatusEnum.APPROVED);

        // Load 5 most recent activity logs
        List<ActivityLog> recentActivities = activityLogRepository.findAll(
                PageRequest.of(0, 5, Sort.by("createdAt").descending())).getContent();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("processingCount", processingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("recentActivities", recentActivities);

        return "admin/admin_dashboard";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/admin/login?logout";
    }

    // Moved to AdminUserManagementController
    // @GetMapping("/users")
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    // public String userManagement(Model model, Authentication authentication) {
    // model.addAttribute("username", authentication.getName());
    // return "admin/user_management";
    // }

    // @GetMapping("/users/{id}/detail")
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    // public String
    // userDetail(@org.springframework.web.bind.annotation.PathVariable Long id,
    // @RequestParam String type,
    // Model model,
    // Authentication authentication) {
    // model.addAttribute("username", authentication.getName());
    // model.addAttribute("userId", id);
    // model.addAttribute("userType", type);
    // return "admin/user_detail";
    // }

    // Moved to AdminDepartmentController
    // @GetMapping("/departments")
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    // public String departmentManagement(Model model, Authentication
    // authentication) {
    // model.addAttribute("username", authentication.getName());
    // return "admin/department_management";
    // }
}