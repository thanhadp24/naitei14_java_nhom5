package vn.sun.public_service_manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.repository.CitizenRepository;
import vn.sun.public_service_manager.service.JwtService;
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
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager; // Dùng để xác thực
    @Autowired
    private JwtService jwtService; // Dùng để tạo token

    // 1. API Đăng ký
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerCitizen(@RequestBody CitizenRegistrationDto registrationDto) {
        if (citizenRepository.existsByNationalId(registrationDto.getNationalId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "message", "CMND/CCCD đã tồn tại trong hệ thống",
                        "field", "nationalId"
                    ));
        }
        if (citizenRepository.existsByEmail(registrationDto.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "success", false,
                        "message", "Email đã được sử dụng",
                        "field", "email"
                    ));
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
        citizen.setPassword(passwordEncoder.encode(registrationDto.getPassword())); // MÃ HÓA

        Citizen savedCitizen = citizenRepository.save(citizen);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "success", true,
                    "message", "Đăng ký thành công! Vui lòng đăng nhập.",
                    "data", Map.of(
                        "nationalId", savedCitizen.getNationalId(),
                        "fullName", savedCitizen.getFullName(),
                        "email", savedCitizen.getEmail()
                    )
                ));
    }

    // 2. API Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto loginDto) {
        // AuthenticationManager gọi CombinedUserDetailsService
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getNationalId(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Tạo JWT Token chứa nationalId và Role (CITIZEN)
        String token = jwtService.generateToken(loginDto.getNationalId());

        return ResponseEntity.ok(new JwtAuthResponse(token));
    }

    // 3. API Đăng xuất
    @GetMapping("/logout")
    public ResponseEntity<String> logout() {
        // Logout chỉ là hướng dẫn client xóa token.
        return ResponseEntity.ok("Logout successful. Client must delete the JWT token.");
    }
}