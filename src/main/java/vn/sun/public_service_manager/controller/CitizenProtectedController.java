package vn.sun.public_service_manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
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
            @Valid @RequestBody CitizenProfileUpdateRequest request) {

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
    public ResponseEntity<Page<ApplicationDTO>> listApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Số trang (bắt đầu từ 0)", schema = @Schema(type = "integer", defaultValue = "0", example = "0"))
            @PageableDefault(size = 10, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String nationalId = getNationalId(userDetails);

        Page<ApplicationDTO> applicationPage = applicationService.getApplicationsByCitizen(nationalId, pageable);

        return ResponseEntity.ok(applicationPage);
    }
}