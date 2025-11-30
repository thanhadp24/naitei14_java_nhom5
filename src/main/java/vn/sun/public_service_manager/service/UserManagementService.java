package vn.sun.public_service_manager.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import vn.sun.public_service_manager.dto.UserFilterDTO;
import vn.sun.public_service_manager.dto.UserListDTO;

public interface UserManagementService {
    
    /**
     * Lấy danh sách tất cả người dùng (Users + Citizens) với filter và phân trang
     * 
     * @param filter - Các tiêu chí lọc (type, role, search, active, departmentId)
     * @param pageable - Thông tin phân trang và sắp xếp
     * @return Page<UserListDTO> - Danh sách người dùng đã phân trang
     */
    Page<UserListDTO> getAllUsers(UserFilterDTO filter, Pageable pageable);
    
    /**
     * Lấy thông tin chi tiết một người dùng
     * 
     * @param id - ID của người dùng
     * @param type - Loại người dùng: USER hoặc CITIZEN
     * @return UserListDTO - Thông tin người dùng
     */
    UserListDTO getUserById(Long id, String type);
}
