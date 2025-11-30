package vn.sun.public_service_manager.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.sun.public_service_manager.dto.UserFilterDTO;
import vn.sun.public_service_manager.dto.UserListDTO;
import vn.sun.public_service_manager.service.UserManagementService;
import vn.sun.public_service_manager.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    /**
     * API lấy danh sách tất cả người dùng (Users + Citizens)
     * Chỉ ADMIN mới có quyền truy cập
     * 
     * @param type - Loại người dùng: USER, CITIZEN, ALL (default: ALL)
     * @param role - Lọc theo vai trò: ADMIN, STAFF (chỉ áp dụng cho User)
     * @param search - Tìm kiếm theo tên, email, phone, nationalId
     * @param active - Lọc theo trạng thái: true/false (chỉ áp dụng cho User)
     * @param departmentId - Lọc theo phòng ban (chỉ áp dụng cho User)
     * @param page - Số trang (default: 0)
     * @param size - Kích thước trang (default: 20)
     * @param sortBy - Sắp xếp theo trường (default: createdAt)
     * @param sortDir - Hướng sắp xếp: ASC, DESC (default: DESC)
     * @return Page<UserListDTO> - Danh sách người dùng đã phân trang
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Lấy thông tin người dùng thành công")
    public ResponseEntity<UserListDTO> getUserById(
            @PathVariable Long id,
            @RequestParam String type
    ) {
        UserListDTO user = userManagementService.getUserById(id, type);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Lấy danh sách người dùng thành công")
    public ResponseEntity<Page<UserListDTO>> getAllUsers(
            @RequestParam(required = false, defaultValue = "ALL") String type,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        UserFilterDTO filter = new UserFilterDTO(type, role, search, active, departmentId);
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserListDTO> users = userManagementService.getAllUsers(filter, pageable);
        
        return ResponseEntity.ok(users);
    }
}
