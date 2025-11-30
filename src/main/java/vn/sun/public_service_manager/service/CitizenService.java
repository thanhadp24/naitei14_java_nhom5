package vn.sun.public_service_manager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import vn.sun.public_service_manager.dto.CitizenProfileResponse;
import vn.sun.public_service_manager.dto.CitizenProfileUpdateRequest;
import vn.sun.public_service_manager.dto.ChangePasswordRequest;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.repository.CitizenRepository;
import vn.sun.public_service_manager.exception.ResourceNotFoundException;
import vn.sun.public_service_manager.exception.InvalidCredentialsException;
import vn.sun.public_service_manager.exception.EmailAlreadyExistsException;


@Service
@RequiredArgsConstructor
public class CitizenService {

    private final CitizenRepository citizenRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional(readOnly = true)
    public CitizenProfileResponse getProfile(String nationalId) {
        Citizen citizen = citizenRepository.findByNationalId(nationalId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen", "National ID", nationalId));

        return CitizenProfileResponse.builder()
                .nationalId(citizen.getNationalId())
                .fullName(citizen.getFullName())
                .dob(citizen.getDob())
                .gender(citizen.getGender())
                .address(citizen.getAddress())
                .phone(citizen.getPhone())
                .email(citizen.getEmail())
                .build();
    }

    @Transactional
    public CitizenProfileResponse updateProfile(String nationalId, CitizenProfileUpdateRequest request) {
        Citizen citizen = citizenRepository.findByNationalId(nationalId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen", "National ID", nationalId));

        citizen.setFullName(request.getFullName());
        citizen.setDob(request.getDob());
        citizen.setGender(request.getGender());
        citizen.setAddress(request.getAddress());
        citizen.setPhone(request.getPhone());

        if (request.getEmail() != null && !request.getEmail().equals(citizen.getEmail())) {
            if (citizenRepository.existsByEmail(request.getEmail())) {
                throw new EmailAlreadyExistsException("Email này đã được sử dụng bởi người khác.");
            }
            citizen.setEmail(request.getEmail());
        }

        Citizen updatedCitizen = citizenRepository.save(citizen);

        return getProfile(updatedCitizen.getNationalId());
    }

    @Transactional
    public void changePassword(String nationalId, ChangePasswordRequest request) {
        Citizen citizen = citizenRepository.findByNationalId(nationalId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen", "National ID", nationalId));

        if (!passwordEncoder.matches(request.getOldPassword(), citizen.getPassword())) {
            throw new InvalidCredentialsException("Mật khẩu cũ không chính xác.");
        }

        citizen.setPassword(passwordEncoder.encode(request.getNewPassword()));

        citizenRepository.save(citizen);
    }
}