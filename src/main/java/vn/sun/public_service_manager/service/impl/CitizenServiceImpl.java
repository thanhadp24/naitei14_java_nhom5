package vn.sun.public_service_manager.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import vn.sun.public_service_manager.dto.CitizenProfileResponse;
import vn.sun.public_service_manager.dto.CitizenProfileUpdateRequest;
import vn.sun.public_service_manager.dto.ChangePasswordRequest;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.repository.CitizenRepository;
import vn.sun.public_service_manager.service.CitizenService;
import vn.sun.public_service_manager.exception.ResourceNotFoundException;
import vn.sun.public_service_manager.exception.InvalidCredentialsException;
import vn.sun.public_service_manager.exception.EmailAlreadyExistsException;

@Service
@RequiredArgsConstructor
public class CitizenServiceImpl implements CitizenService {

    private final CitizenRepository citizenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
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
    @Override
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
    @Override
    public void changePassword(String nationalId, ChangePasswordRequest request) {
        Citizen citizen = citizenRepository.findByNationalId(nationalId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen", "National ID", nationalId));

        if (!passwordEncoder.matches(request.getOldPassword(), citizen.getPassword())) {
            throw new InvalidCredentialsException("Mật khẩu cũ không chính xác.");
        }

        citizen.setPassword(passwordEncoder.encode(request.getNewPassword()));

        citizenRepository.save(citizen);
    }

    @Override
    public Page<Citizen> getAll(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size);
        if (keyword != null && !keyword.isEmpty()) {
            return citizenRepository.findByKeyword(keyword, pageable);
        } else {
            return citizenRepository.findAll(pageable);
        }
    }

    @Override
    public Citizen getById(Long id) {
        return citizenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen not found with id: " + id));
    }

    @Override
    public Citizen save(Citizen citizen) {
        if (citizen.getId() == null) {
            if (citizenRepository.existsByNationalId(citizen.getNationalId())) {
                throw new EmailAlreadyExistsException("Công dân với CMND/CCCD đã tồn tại.");
            }
            if (citizenRepository.existsByEmail(citizen.getEmail())) {
                throw new EmailAlreadyExistsException("Công dân với email đã tồn tại.");
            }
            if (citizenRepository.existsByPhone(citizen.getPhone())) {
                throw new EmailAlreadyExistsException("Công dân với số điện thoại đã tồn tại.");
            }
            citizen.setPassword(passwordEncoder.encode(citizen.getPassword()));
            return citizenRepository.save(citizen);
        } else {
            Citizen citizenInDb = citizenRepository.findById(citizen.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Citizen not found with id: " + citizen.getId()));
            citizenInDb.setFullName(citizen.getFullName());
            citizenInDb.setDob(citizen.getDob());
            citizenInDb.setGender(citizen.getGender());
            citizenInDb.setAddress(citizen.getAddress());
            citizenInDb.setPhone(citizen.getPhone());
            citizenInDb.setEmail(citizen.getEmail());
            return citizenRepository.save(citizenInDb);
        }
    }

    @Override
    public void deleteById(Long id) {
        if (!citizenRepository.existsById(id)) {
            throw new ResourceNotFoundException("Citizen not found with id: " + id);
        }
        citizenRepository.deleteById(id);
    }
}
