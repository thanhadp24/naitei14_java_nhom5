package vn.sun.public_service_manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.sun.public_service_manager.dto.ServiceDTO;
import vn.sun.public_service_manager.dto.ServicePageResponse;
import vn.sun.public_service_manager.service.ServiceService;
import vn.sun.public_service_manager.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Services", description = "APIs xem thông tin dịch vụ công (không cần đăng nhập)")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @GetMapping
    @ApiMessage("Lấy danh sách dịch vụ thành công")
    @Operation(summary = "Lấy danh sách dịch vụ", description = "Lấy danh sách dịch vụ công với filter, search, pagination")
    public ResponseEntity<ServicePageResponse> getAllServices(
            @Parameter(description = "Số trang (bắt đầu từ 1)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Số dịch vụ mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo field", example = "id") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Chiều sắp xếp: asc hoặc desc", example = "asc") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Tìm kiếm theo tên dịch vụ hoặc mã", example = "") @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo ID loại dịch vụ (để trống = lấy tất cả)", example = "") @RequestParam(required = false) Long serviceTypeId) {

        if (page < 1)
            page = 1;
        ServicePageResponse response;
        if (serviceTypeId != null && keyword != null && !keyword.isEmpty()) {
            // Filter theo cả serviceTypeId và keyword
            response = serviceService.searchByServiceTypeAndKeyword(serviceTypeId, keyword, page, size);
        } else if (serviceTypeId != null) {
            // Chỉ filter theo serviceTypeId
            response = serviceService.searchByServiceType(serviceTypeId, page, size);
        } else {
            // Không filter hoặc chỉ search keyword
            response = serviceService.getAllServices(page, size, sortBy, sortDir, keyword);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @ApiMessage("Lấy chi tiết dịch vụ thành công")
    @Operation(summary = "Xem chi tiết dịch vụ", description = "Lấy thông tin chi tiết một dịch vụ theo ID")
    public ResponseEntity<?> getServiceById(
            @Parameter(description = "ID của dịch vụ", example = "1") @PathVariable Long id) {
        ServiceDTO serviceDTO = serviceService.getServiceById(id);
        return ResponseEntity.ok(serviceDTO);
    }
}
