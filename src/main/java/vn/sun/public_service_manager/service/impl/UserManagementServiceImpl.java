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
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.sun.public_service_manager.dto.UserCreateDTO;
import vn.sun.public_service_manager.dto.UserFilterDTO;
import vn.sun.public_service_manager.dto.UserListDTO;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.entity.Department;
import vn.sun.public_service_manager.entity.Gender;
import vn.sun.public_service_manager.entity.Role;
import vn.sun.public_service_manager.entity.User;
import vn.sun.public_service_manager.repository.CitizenRepository;
import vn.sun.public_service_manager.repository.DepartmentRepository;
import vn.sun.public_service_manager.repository.RoleRepository;
import vn.sun.public_service_manager.repository.UserRespository;
import vn.sun.public_service_manager.service.UserManagementService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRespository userRepository;
    private final CitizenRepository citizenRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

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

    @Override
    public Long createUser(UserCreateDTO dto, String type) {
        type = type != null ? type.toUpperCase() : "USER";
        
        if (type.equals("USER")) {
            // Check username exists
            if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
                throw new RuntimeException("Username đã tồn tại");
            }
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new RuntimeException("Email đã tồn tại");
            }
            
            User user = new User();
            user.setUsername(dto.getUsername());
            user.setEmail(dto.getEmail());
            user.setPhone(dto.getPhone());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setAddress(dto.getAddress());
            user.setActive(dto.getActive() != null ? dto.getActive() : true);
            
            // Set Department
            if (dto.getDepartmentId() != null) {
                Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Phòng ban không tồn tại"));
                user.setDepartment(department);
            }
            
            // Set Roles
            if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
                Set<Role> roles = new HashSet<>();
                for (Long roleId : dto.getRoleIds()) {
                    Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại với ID: " + roleId));
                    roles.add(role);
                }
                user.setRoles(roles);
            }
            
            User savedUser = userRepository.save(user);
            return savedUser.getId();
            
        } else if (type.equals("CITIZEN")) {
            // Check nationalId exists
            if (citizenRepository.existsByNationalId(dto.getNationalId())) {
                throw new RuntimeException("CMND/CCCD đã tồn tại");
            }
            if (citizenRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email đã tồn tại");
            }
            
            Citizen citizen = new Citizen();
            citizen.setFullName(dto.getFullName());
            citizen.setNationalId(dto.getNationalId());
            citizen.setEmail(dto.getEmail());
            citizen.setPhone(dto.getPhone());
            citizen.setPassword(passwordEncoder.encode(dto.getPassword()));
            citizen.setAddress(dto.getAddress());
            citizen.setGender(Gender.valueOf(dto.getGender()));
            
            // Parse date
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date dob = sdf.parse(dto.getDateOfBirth());
                citizen.setDob(dob);
            } catch (Exception e) {
                throw new RuntimeException("Định dạng ngày sinh không hợp lệ");
            }
            
            Citizen savedCitizen = citizenRepository.save(citizen);
            return savedCitizen.getId();
            
        } else {
            throw new RuntimeException("Loại người dùng không hợp lệ");
        }
    }

    @Override
    public void updateUser(Long id, UserCreateDTO dto, String type) {
        type = type != null ? type.toUpperCase() : "USER";
        
        if (type.equals("USER")) {
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
            
            // Check username exists (exclude current user)
            userRepository.findByUsername(dto.getUsername()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(id)) {
                    throw new RuntimeException("Username đã tồn tại");
                }
            });
            
            // Check email exists (exclude current user)
            userRepository.findByEmail(dto.getEmail()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(id)) {
                    throw new RuntimeException("Email đã tồn tại");
                }
            });
            
            user.setUsername(dto.getUsername());
            user.setEmail(dto.getEmail());
            user.setPhone(dto.getPhone());
            user.setAddress(dto.getAddress());
            
            // Only update password if provided
            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
            
            // Update Department
            if (dto.getDepartmentId() != null) {
                Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Phòng ban không tồn tại"));
                user.setDepartment(department);
            } else {
                user.setDepartment(null);
            }
            
            // Update Roles
            if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
                Set<Role> roles = new HashSet<>();
                for (Long roleId : dto.getRoleIds()) {
                    Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new RuntimeException("Vai trò không tồn tại với ID: " + roleId));
                    roles.add(role);
                }
                user.setRoles(roles);
            }
            
            userRepository.save(user);
        } else {
            throw new RuntimeException("Chỉ hỗ trợ cập nhật USER");
        }
    }

    @Override
    public void deleteUser(Long id, String type) {
        type = type != null ? type.toUpperCase() : "USER";
        
        if (type.equals("USER")) {
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
            userRepository.delete(user);
        } else {
            throw new RuntimeException("Chỉ hỗ trợ xóa USER");
        }
    }

    @Override
    public void toggleUserActive(Long id, boolean active, String type) {
        type = type != null ? type.toUpperCase() : "USER";
        
        if (type.equals("USER")) {
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
            user.setActive(active);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Chỉ hỗ trợ khóa/mở khóa USER");
        }
    }
}
