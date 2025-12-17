package vn.sun.public_service_manager.service;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import vn.sun.public_service_manager.dto.UserFilterDTO;
import vn.sun.public_service_manager.dto.UserListDTO;

public interface UserManagementService {

    /**
     * Lấy danh sách tất cả người dùng (Users + Citizens) với filter và phân trang
     * 
     * @param filter   - Các tiêu chí lọc (type, role, search, active, departmentId)
     * @param pageable - Thông tin phân trang và sắp xếp
     * @return Page<UserListDTO> - Danh sách người dùng đã phân trang
     */
    Page<UserListDTO> getAllUsers(UserFilterDTO filter, Pageable pageable);

    /**
     * Lấy danh sách tất cả users (không phân trang) để hiển thị trong dropdown
     * 
     * @return List<UserListDTO> - Danh sách tất cả users
     */
    java.util.List<UserListDTO> getAllUsersForSelection();

    /**
     * Lấy danh sách users theo phòng ban để hiển thị trong dropdown
     * 
     * @param departmentId - ID của phòng ban
     * @return List<UserListDTO> - Danh sách users thuộc phòng ban
     */
    java.util.List<UserListDTO> getUsersByDepartmentId(Long departmentId);

    /**
     * Lấy thông tin chi tiết một người dùng
     * 
     * @param id   - ID của người dùng
     * @param type - Loại người dùng: USER hoặc CITIZEN
     * @return UserListDTO - Thông tin người dùng
     */
    UserListDTO getUserById(Long id, String type);

    /**
     * Tạo người dùng mới
     * 
     * @param dto  - Thông tin người dùng
     * @param type - Loại: USER hoặc CITIZEN
     * @return Long - ID của người dùng mới tạo
     */
    Long createUser(vn.sun.public_service_manager.dto.UserCreateDTO dto, String type);

    /**
     * Cập nhật thông tin người dùng
     * 
     * @param id   - ID của người dùng
     * @param dto  - Thông tin cập nhật
     * @param type - Loại: USER hoặc CITIZEN
     */
    void updateUser(Long id, vn.sun.public_service_manager.dto.UserCreateDTO dto, String type);

    /**
     * Xóa người dùng
     * 
     * @param id   - ID của người dùng
     * @param type - Loại: USER hoặc CITIZEN
     */
    void deleteUser(Long id, String type);

    /**
     * Khóa/Mở khóa tài khoản người dùng
     * 
     * @param id     - ID của người dùng
     * @param active - Trạng thái active (true/false)
     * @param type   - Loại: USER hoặc CITIZEN
     */
    void toggleUserActive(Long id, boolean active, String type);

    void exportStaffToCsv(Writer writer);

    Map<String, Object> importStaffFromCsv(MultipartFile file) throws IOException;

    void exportCitizensToCsv(Writer writer);

    void exportApplicationsToCsv(Writer writer);
    public Map<String, Object> importCitizensFromCsv(MultipartFile file) throws IOException;
}
