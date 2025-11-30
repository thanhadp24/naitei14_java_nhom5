package vn.sun.public_service_manager.controller;

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

@RestController
@RequestMapping("/api/v1/citizen/auth")
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
    public ResponseEntity<String> registerCitizen(@RequestBody CitizenRegistrationDto registrationDto) {
        if (citizenRepository.existsByNationalId(registrationDto.getNationalId())) {
            return new ResponseEntity<>("National ID already exists!", HttpStatus.BAD_REQUEST);
        }
        if (citizenRepository.existsByEmail(registrationDto.getEmail())) {
            return new ResponseEntity<>("Email already exists!", HttpStatus.BAD_REQUEST);
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

        citizenRepository.save(citizen);
        return new ResponseEntity<>("Citizen registered successfully!", HttpStatus.CREATED);
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