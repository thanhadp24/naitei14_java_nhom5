package vn.sun.public_service_manager.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.repository.CitizenRepository;
import vn.sun.public_service_manager.service.CitizenService;
import vn.sun.public_service_manager.service.JwtService;
import vn.sun.public_service_manager.utils.annotation.LogActivity;
import vn.sun.public_service_manager.dto.CitizenRegistrationDto;
import vn.sun.public_service_manager.dto.JwtAuthResponse;
import vn.sun.public_service_manager.dto.LoginDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/citizen/auth")
@Tag(name = "Citizen Auth", description = "Xác thực công dân: Đăng ký, Đăng nhập, Đăng xuất")
public class CitizenAuthController {

        @Autowired
        private CitizenRepository citizenRepository;

        @Autowired
        private CitizenService citizenService;

        @Autowired
        private PasswordEncoder passwordEncoder;
        @Autowired
        private AuthenticationManager authenticationManager; // Dùng để xác thực
        @Autowired
        private JwtService jwtService; // Dùng để tạo token

        // 1. API Đăng ký
        @PostMapping("/register")
        public ResponseEntity<Map<String, Object>> registerCitizen(
                        @RequestBody @Valid CitizenRegistrationDto registrationDto) {
                if (citizenRepository.existsByNationalId(registrationDto.getNationalId())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(Map.of(
                                                        "success", false,
                                                        "message", "CMND/CCCD đã tồn tại trong hệ thống",
                                                        "field", "nationalId"));
                }
                if (citizenRepository.existsByEmail(registrationDto.getEmail())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(Map.of(
                                                        "success", false,
                                                        "message", "Email đã được sử dụng",
                                                        "field", "email"));
                }

                Citizen citizen = new Citizen();
                // Ánh xạ DTO sang Entity
                citizen.setNationalId(registrationDto.getNationalId());
                citizen.setFullName(registrationDto.getFullName());
                citizen.setDob(registrationDto.getDob());
                citizen.setGender(registrationDto.getGender());
                citizen.setAddress(registrationDto.getAddress());
                citizen.setPhone(registrationDto.getPhone());
                citizen.setEmail(registrationDto.getEmail());
                citizen.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
                // // MÃ HÓA

                Citizen savedCitizen = citizenService.save(citizen);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(Map.of(
                                                // "success", true,
                                                // "message", "Đăng ký thành công! Vui lòng đăng nhập.",
                                                "citizen", Map.of(
                                                                "nationalId", savedCitizen.getNationalId(),
                                                                "fullName", savedCitizen.getFullName(),
                                                                "email", savedCitizen.getEmail())));
        }

        // 2. API Đăng nhập
        @LogActivity(action = "Citizen Login", targetType = "CITIZEN AUTH", description = "Đăng nhập hệ thống công dân")
        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
                try {
                        Authentication authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(loginDto.getNationalId(),
                                                        loginDto.getPassword()));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        String token = jwtService.generateToken(loginDto.getNationalId());
                        return ResponseEntity.ok(new JwtAuthResponse(token));
                } catch (org.springframework.security.authentication.DisabledException ex) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("success", false, "message", "Tài khoản đã bị vô hiệu hóa"));
                } catch (org.springframework.security.authentication.LockedException ex) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("success", false, "message", "Tài khoản bị khoá"));
                } catch (org.springframework.security.authentication.BadCredentialsException ex) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of("success", false, "message",
                                                        "Tên đăng nhập hoặc mật khẩu không đúng"));
                } catch (org.springframework.security.core.AuthenticationException ex) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of("success", false, "message", "Xác thực thất bại"));
                } catch (Exception ex) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(Map.of("success", false, "message", "Lỗi server"));
                }
        }

        // 3. API Đăng xuất
        @LogActivity(action = "Citizen Logout", targetType = "CITIZEN AUTH", description = "Đăng xuất hệ thống công dân")
        @GetMapping("/logout")
        public ResponseEntity<String> logout() {
                // Logout chỉ là hướng dẫn client xóa token.
                return ResponseEntity.ok("Logout successful. Client must delete the JWT token.");
        }
}