package vn.sun.public_service_manager.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.sun.public_service_manager.service.ServiceManagementService;
import vn.sun.public_service_manager.utils.annotation.ApiMessage;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/services")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
public class ServiceApiController {

    private final ServiceManagementService serviceManagementService;

    @GetMapping("/export")
    @ApiMessage("Xuất danh sách dịch vụ ra file CSV thành công")
    public void exportServicesToCsv(HttpServletResponse response) {
        try {
            response.setContentType("text/csv; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"services_" + System.currentTimeMillis() + ".csv\"");

            Writer writer = new OutputStreamWriter(
                    response.getOutputStream(),
                    StandardCharsets.UTF_8);

            serviceManagementService.exportServicesToCsv(writer);

            writer.flush();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xuất file CSV", e);
        }
    }

    @PostMapping("/import")
    @ApiMessage("Nhập danh sách dịch vụ từ file CSV thành công")
    public ResponseEntity<Map<String, Object>> importServicesFromCsv(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "File không được để trống!"));
        }

        if (!file.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Chỉ chấp nhận file CSV!"));
        }

        try {
            Map<String, Object> result = serviceManagementService.importServicesFromCsv(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
