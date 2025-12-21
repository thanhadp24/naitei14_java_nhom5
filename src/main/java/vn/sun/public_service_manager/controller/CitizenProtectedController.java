package vn.sun.public_service_manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import vn.sun.public_service_manager.dto.ApiResponseDTO;
import vn.sun.public_service_manager.dto.ApplicationDTO;
import vn.sun.public_service_manager.dto.CitizenProfileResponse;
import vn.sun.public_service_manager.dto.CitizenProfileUpdateRequest;
import vn.sun.public_service_manager.dto.ChangePasswordRequest;
import vn.sun.public_service_manager.service.ApplicationService;
import vn.sun.public_service_manager.service.CitizenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/citizen")
@RequiredArgsConstructor
@Tag(name = "Citizen Protected", description = "APIs cần đăng nhập cho công dân")
public class CitizenProtectedController {

    private final CitizenService citizenService;
    private final ApplicationService applicationService;

    private String getNationalId(UserDetails userDetails) {
        return userDetails.getUsername();
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/me")
    public ResponseEntity<CitizenProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        String nationalId = getNationalId(userDetails);

        CitizenProfileResponse profile = citizenService.getProfile(nationalId);
        return ResponseEntity.ok(profile);
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PutMapping("/update")
    public ResponseEntity<CitizenProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid CitizenProfileUpdateRequest request) {

        String nationalId = getNationalId(userDetails);
        CitizenProfileResponse updatedProfile = citizenService.updateProfile(nationalId, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {

        String nationalId = getNationalId(userDetails);
        citizenService.changePassword(nationalId, request);

        return ResponseEntity.ok("Mật khẩu đã được thay đổi thành công.");
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/applications")
    @Operation(summary = "Lấy danh sách hồ sơ của tôi", description = "Lấy tất cả hồ sơ đã nộp")
    public ResponseEntity<ApiResponseDTO<Object>> listApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Số trang (bắt đầu từ 1)", schema = @Schema(type = "integer", defaultValue = "1", example = "1")) @PageableDefault(size = 10, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String nationalId = getNationalId(userDetails);

        Page<ApplicationDTO> applicationPage = applicationService.getApplicationsByCitizen(nationalId, pageable);

        // Chuẩn hóa dữ liệu trả về với metadata phân trang
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("content", applicationPage.getContent());
        data.put("currentPage", applicationPage.getNumber() + 1); // Page index + 1
        data.put("totalPages", applicationPage.getTotalPages());
        data.put("totalElements", applicationPage.getTotalElements());
        data.put("size", applicationPage.getSize());
        data.put("hasNext", applicationPage.hasNext());
        data.put("hasPrevious", applicationPage.hasPrevious());

        ApiResponseDTO<Object> res = new ApiResponseDTO<>();
        res.setMessage("Lấy danh sách dịch vụ thành công");
        res.setData(data);
        res.setStatus(HttpStatus.OK.value());
        res.setError(null);

        return ResponseEntity.ok(res);
    }
}