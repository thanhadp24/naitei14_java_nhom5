package vn.sun.public_service_manager.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.sun.public_service_manager.dto.UserFilterDTO;
import vn.sun.public_service_manager.dto.UserListDTO;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.entity.Role;
import vn.sun.public_service_manager.entity.User;
import vn.sun.public_service_manager.repository.CitizenRepository;
import vn.sun.public_service_manager.repository.UserRespository;
import vn.sun.public_service_manager.service.UserManagementService;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRespository userRepository;
    private final CitizenRepository citizenRepository;

    @Override
    public Page<UserListDTO> getAllUsers(UserFilterDTO filter, Pageable pageable) {
        List<UserListDTO> allUsers = new ArrayList<>();
        
        String type = filter.getType() != null ? filter.getType().toUpperCase() : "ALL";
        
        // Lấy danh sách Users nếu cần
        if (type.equals("ALL") || type.equals("USER")) {
            Page<User> users = userRepository.findWithFilters(
                filter.getSearch(),
                filter.getActive(),
                filter.getRole(),
                filter.getDepartmentId(),
                Pageable.unpaged()
            );
            
            List<UserListDTO> userDTOs = users.getContent().stream()
                .map(this::convertUserToDTO)
                .collect(Collectors.toList());
            allUsers.addAll(userDTOs);
        }
        
        // Lấy danh sách Citizens nếu cần
        if (type.equals("ALL") || type.equals("CITIZEN")) {
            Page<Citizen> citizens = citizenRepository.findWithSearch(
                filter.getSearch(),
                Pageable.unpaged()
            );
            
            List<UserListDTO> citizenDTOs = citizens.getContent().stream()
                .map(this::convertCitizenToDTO)
                .collect(Collectors.toList());
            allUsers.addAll(citizenDTOs);
        }
        
        // Sắp xếp theo createdAt DESC (mới nhất trước)
        allUsers.sort(Comparator.comparing(UserListDTO::getCreatedAt, 
            Comparator.nullsLast(Comparator.reverseOrder())));
        
        // Áp dụng phân trang thủ công
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allUsers.size());
        
        List<UserListDTO> pageContent = start < allUsers.size() 
            ? allUsers.subList(start, end) 
            : Collections.emptyList();
        
        return new PageImpl<>(pageContent, pageable, allUsers.size());
    }

    private UserListDTO convertUserToDTO(User user) {
        LocalDateTime createdAt = user.getCreatedAt();
        
        return UserListDTO.builder()
            .id(user.getId())
            .type("USER")
            .username(user.getUsername())
            .fullName(user.getUsername()) // User không có fullName, dùng username
            .email(user.getEmail())
            .phone(user.getPhone())
            .address(user.getAddress())
            .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
            .roles(user.getRoles() != null 
                ? user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                : Collections.emptySet())
            .active(user.getActive())
            .createdAt(createdAt)
            .build();
    }

    private UserListDTO convertCitizenToDTO(Citizen citizen) {
        // Convert Date to LocalDateTime
        LocalDateTime createdAt = citizen.getCreatedAt() != null
            ? citizen.getCreatedAt().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
            : null;
        
        return UserListDTO.builder()
            .id(citizen.getId())
            .type("CITIZEN")
            .nationalId(citizen.getNationalId())
            .fullName(citizen.getFullName())
            .email(citizen.getEmail())
            .phone(citizen.getPhone())
            .address(citizen.getAddress())
            .dateOfBirth(citizen.getDob())
            .gender(citizen.getGender() != null ? citizen.getGender().name() : null)
            .createdAt(createdAt)
            .build();
    }

    @Override
    public UserListDTO getUserById(Long id, String type) {
        type = type != null ? type.toUpperCase() : "USER";
        
        if (type.equals("USER")) {
            User user = userRepository.findByIdWithRolesAndDepartment(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
            return convertUserToDTO(user);
        } else if (type.equals("CITIZEN")) {
            Citizen citizen = citizenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công dân với ID: " + id));
            return convertCitizenToDTO(citizen);
        } else {
            throw new RuntimeException("Loại người dùng không hợp lệ");
        }
    }
}
