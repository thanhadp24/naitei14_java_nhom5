package vn.sun.public_service_manager.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.sun.public_service_manager.dto.UserCreateDTO;
import vn.sun.public_service_manager.dto.UserFilterDTO;
import vn.sun.public_service_manager.dto.UserListDTO;
import vn.sun.public_service_manager.service.UserManagementService;
import vn.sun.public_service_manager.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Tạo người dùng thành công")
    public ResponseEntity<Long> createUser(
            @RequestParam String type,
            @RequestBody UserCreateDTO dto
    ) {
        Long userId = userManagementService.createUser(dto, type);
        return ResponseEntity.status(HttpStatus.CREATED).body(userId);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Cập nhật người dùng thành công")
    public ResponseEntity<Void> updateUser(
            @PathVariable Long id,
            @RequestParam String type,
            @RequestBody UserCreateDTO dto
    ) {
        userManagementService.updateUser(id, dto, type);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Xóa người dùng thành công")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @RequestParam String type
    ) {
        userManagementService.deleteUser(id, type);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("Thay đổi trạng thái người dùng thành công")
    public ResponseEntity<Void> toggleUserActive(
            @PathVariable Long id,
            @RequestParam String type,
            @RequestParam boolean active
    ) {
        userManagementService.toggleUserActive(id, active, type);
        return ResponseEntity.ok().build();
    }
}
