package vn.sun.public_service_manager.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVWriter;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import vn.sun.public_service_manager.dto.UserCreateDTO;
import vn.sun.public_service_manager.dto.UserFilterDTO;
import vn.sun.public_service_manager.dto.UserListDTO;
import vn.sun.public_service_manager.entity.ApplicationStatus;
import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.entity.Department;
import vn.sun.public_service_manager.entity.Gender;
import vn.sun.public_service_manager.entity.Role;
import vn.sun.public_service_manager.entity.User;
import vn.sun.public_service_manager.repository.ApplicationRepository;
import vn.sun.public_service_manager.repository.CitizenRepository;
import vn.sun.public_service_manager.repository.DepartmentRepository;
import vn.sun.public_service_manager.repository.RoleRepository;
import vn.sun.public_service_manager.repository.UserRespository;
import vn.sun.public_service_manager.service.UserManagementService;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRespository userRepository;
    private final CitizenRepository citizenRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationRepository applicationRepository;

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
                    Pageable.unpaged());

            List<UserListDTO> userDTOs = users.getContent().stream()
                    .map(this::convertUserToDTO)
                    .collect(Collectors.toList());
            allUsers.addAll(userDTOs);
        }

        // Lấy danh sách Citizens nếu cần
        if (type.equals("ALL") || type.equals("CITIZEN")) {
            Page<Citizen> citizens = citizenRepository.findWithSearch(
                    filter.getSearch(),
                    Pageable.unpaged());

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
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .roles(user.getRoles() != null
                        ? user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                        : Collections.emptySet())
                .roleIds(user.getRoles() != null
                        ? user.getRoles().stream().map(Role::getId).collect(Collectors.toSet())
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

    @Override
    public void exportStaffToCsv(Writer writer) {
        // Admin export tất cả users
        List<User> users = userRepository.findAll();

        try {
            // Write UTF-8 BOM để Excel nhận diện
            writer.write('\ufeff');

            // Header - không có khoảng trắng sau dấu phẩy
            writer.write("ID,Username,Email,Phone,Address,Department,Roles,Active,Created At\n");
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // Write data với escape
            for (User user : users) {
                String roles = user.getRoles() != null
                        ? user.getRoles().stream()
                                .map(Role::getName)
                                .collect(Collectors.joining(";"))
                        : "";

                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%b,%s\n",
                        user.getId(),
                        escapeCSV(user.getUsername()),
                        escapeCSV(user.getEmail()),
                        escapeCSV(user.getPhone()),
                        escapeCSV(user.getAddress()),
                        escapeCSV(user.getDepartment() != null ? user.getDepartment().getName() : ""),
                        escapeCSV(roles),
                        user.getActive(),
                        user.getCreatedAt() != null ? user.getCreatedAt().format(df) : ""));
            }

            writer.flush();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xuất CSV: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Map<String, Object> importStaffFromCsv(MultipartFile file) throws IOException {
        List<String> errors = new ArrayList<>();
        int total = 0;
        int success = 0;
        int skipped = 0;
        int failed = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("File CSV trống");
            }

            String[] headers = headerLine.split(",");
            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                idx.put(headers[i].trim().toLowerCase(), i);
            }

            String line;
            while ((line = br.readLine()) != null) {
                total++;
                String[] cols = parseCSVLine(line);
                try {
                    String username = getColumn(cols, idx, "username");
                    String fullName = getColumn(cols, idx, "fullname");
                    String email = getColumn(cols, idx, "email");
                    String phone = getColumn(cols, idx, "phone");
                    String departmentIdStr = getColumn(cols, idx, "departmentid");
                    String roleName = getColumn(cols, idx, "role");

                    if (username == null || username.isBlank()) {
                        errors.add(String.format("Line %d: username trống", total + 1));
                        failed++;
                        continue;
                    }

                    // skip if user exists
                    if (userRepository.findByUsername(username).isPresent()) {
                        skipped++;
                        continue;
                    }

                    // create user
                    vn.sun.public_service_manager.entity.User user = new vn.sun.public_service_manager.entity.User();
                    user.setUsername(username.trim());
                    user.setEmail(email == null ? null : email.trim());
                    user.setPhone(phone == null ? null : phone.trim());
                    user.setActive(true);

                    // department
                    if (departmentIdStr != null && !departmentIdStr.isBlank()) {
                        try {
                            Long deptId = Long.parseLong(departmentIdStr.trim());
                            departmentRepository.findById(deptId).ifPresent(user::setDepartment);
                        } catch (NumberFormatException nfe) {
                            // ignore invalid dept id, log error
                            errors.add(String.format("Line %d: departmentId không hợp lệ: %s", total + 1,
                                    departmentIdStr));
                        }
                    }

                    // role lookup (support values like ADMIN, STAFF, USER or full ROLE_ form)
                    Role roleEntity = null;
                    if (roleName != null && !roleName.isBlank()) {
                        String r = roleName.trim();
                        // prefer exact match with ROLE_ prefix
                        Optional<Role> op = roleRepository.findByName(r.startsWith("ROLE_") ? r : "ROLE_" + r);
                        if (op.isEmpty()) {
                            op = roleRepository.findByName(r); // try raw
                        }
                        if (op.isPresent()) {
                            roleEntity = op.get();
                        } else {
                            errors.add(String.format("Line %d: role '%s' không tồn tại", total + 1, r));
                        }
                    }

                    if (roleEntity != null) {
                        user.setRoles(Collections.singleton(roleEntity));
                    }

                    // set default password (random) and encode
                    user.setPassword(passwordEncoder.encode(username));

                    // save
                    userRepository.save(user);
                    success++;
                } catch (Exception e) {
                    failed++;
                    errors.add(String.format("Line %d: Lỗi khi xử lý: %s", total + 1, e.getMessage()));
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("success", success);
        result.put("skipped", skipped);
        result.put("failed", failed);
        result.put("errors", errors);
        return result;
    }

    // Helper method để escape CSV
    private String escapeCSV(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        // Nếu có dấu phẩy, dấu ngoặc kép, xuống dòng -> wrap bằng quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            // Escape dấu ngoặc kép bằng cách double nó
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }

    private String getColumn(String[] cols, Map<String, Integer> idx, String name) {
        Integer i = idx.get(name.toLowerCase());
        if (i == null)
            return null;
        if (i >= cols.length)
            return null;
        String v = cols[i].trim();
        return v.isEmpty() ? null : v;
    }

    // simple CSV split that respects quoted fields
    private String[] parseCSVLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    @Override
    public List<UserListDTO> getAllUsersForSelection() {
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.ASC, "username"));
        return users.stream()
                .map(user -> UserListDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<UserListDTO> getUsersByDepartmentId(Long departmentId) {
        List<User> users = userRepository.findWithFilters(null, true, null, departmentId, Pageable.unpaged())
                .getContent();
        return users.stream()
                .map(user -> UserListDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> importCitizensFromCsv(MultipartFile file) throws IOException {
        List<String> errors = new ArrayList<>();
        List<String> success = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        int rowNumber = 0;
        int totalRows = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // 1. Skip BOM if present
            reader.mark(1);
            if (reader.read() != 0xFEFF) {
                reader.reset();
            }

            // 2. Read first line as header
            String headerLine = reader.readLine();
            if (headerLine == null)
                throw new RuntimeException("File CSV rỗng!");

            // 3. Validate header (theo cấu trúc Entity Citizen)
            String expectedHeader = "fullName,dob,gender,nationalId,address,phone,email";
            if (!headerLine.toLowerCase().startsWith(expectedHeader.toLowerCase())) {
                throw new RuntimeException("File CSV không đúng định dạng! Cần có: " + expectedHeader);
            }

            // 4. Read data lines
            String line;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.trim().isEmpty())
                    continue;
                totalRows++;

                try {
                    String[] values = parseCSVLine(line);
                    if (values.length < 7) {
                        errors.add("Dòng " + rowNumber + ": Thiếu dữ liệu (Cần đủ 7 cột)");
                        continue;
                    }

                    // Map values
                    String fullName = values[0].trim();
                    String dobStr = values[1].trim();
                    String genderStr = values[2].trim().toUpperCase();
                    String nationalId = values[3].trim();
                    String address = values[4].trim();
                    String phone = values[5].trim();
                    String email = values[6].trim();

                    // --- VALIDATION ---
                    if (fullName.isEmpty() || nationalId.isEmpty() || email.isEmpty()) {
                        errors.add("Dòng " + rowNumber + ": Họ tên, CCCD và Email không được để trống");
                        continue;
                    }

                    // Check duplicate CCCD
                    if (citizenRepository.existsByNationalId(nationalId)) {
                        errors.add("Dòng " + rowNumber + ": Số CCCD '" + nationalId + "' đã tồn tại");
                        continue;
                    }

                    // Check duplicate Email
                    if (citizenRepository.existsByEmail(email)) {
                        errors.add("Dòng " + rowNumber + ": Email '" + email + "' đã tồn tại");
                        continue;
                    }

                    // Parse Date of Birth
                    Date dob;
                    try {
                        dob = dateFormat.parse(dobStr);
                    } catch (ParseException e) {
                        errors.add("Dòng " + rowNumber + ": Ngày sinh '" + dobStr + "' sai định dạng yyyy-MM-dd");
                        continue;
                    }

                    // Parse Gender Enum
                    Gender gender;
                    try {
                        gender = Gender.valueOf(genderStr);
                    } catch (IllegalArgumentException e) {
                        errors.add("Dòng " + rowNumber + ": Giới tính '" + genderStr
                                + "' không hợp lệ (MALE/FEMALE/OTHER)");
                        continue;
                    }

                    // --- CREATE CITIZEN ---
                    Citizen citizen = new Citizen();
                    citizen.setFullName(fullName);
                    citizen.setDob(dob);
                    citizen.setGender(gender);
                    citizen.setNationalId(nationalId);
                    citizen.setAddress(address);
                    citizen.setPhone(phone);
                    citizen.setEmail(email);

                    // Mật khẩu mặc định là NationalId
                    citizen.setPassword(passwordEncoder.encode(nationalId));

                    citizenRepository.save(citizen);
                    success.add("Dòng " + rowNumber + ": Import công dân '" + fullName + "' thành công");

                } catch (Exception e) {
                    errors.add("Dòng " + rowNumber + ": " + e.getMessage());
                }
            }
        }

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("total", totalRows);
        response.put("success", success.size());
        response.put("failed", errors.size());
        response.put("errors", errors);
        response.put("successMessages", success);

        return response;
    }

    @Override
    public void exportApplicationsToCsv(Writer writer,
            org.springframework.security.core.Authentication authentication) {
        try {
            // 1. Ghi ký tự BOM để hỗ trợ hiển thị tiếng Việt trong Excel
            writer.write('\ufeff');

            try (CSVWriter csvWriter = new CSVWriter(writer)) {
                // Định nghĩa Header cho báo cáo hồ sơ
                String[] header = {
                        "Mã hồ sơ",
                        "Tên dịch vụ",
                        "Tên công dân",
                        "Trạng thái hiện tại",
                        "Ngày nộp",
                        "Ghi chú",
                        "Thời gian xử lý dự kiến (Ngày)"
                };
                csvWriter.writeNext(header);

                // 2. Xác định role và apply filter
                vn.sun.public_service_manager.dto.ApplicationFilterDTO filter = new vn.sun.public_service_manager.dto.ApplicationFilterDTO();

                boolean isManager = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

                if (isManager) {
                    // Nếu là MANAGER, chỉ lấy applications của department của họ
                    String username = authentication.getName();
                    vn.sun.public_service_manager.entity.User currentUser = userRepository.findByUsername(username)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    if (currentUser.getDepartment() != null) {
                        filter.setDepartmentId(currentUser.getDepartment().getId());
                    }
                }
                // ADMIN sẽ không có filter, lấy tất cả

                // 3. Lấy dữ liệu hồ sơ với filter
                org.springframework.data.jpa.domain.Specification<vn.sun.public_service_manager.entity.Application> spec = vn.sun.public_service_manager.repository.specification.ApplicationSpecification
                        .filterApplications(filter);
                List<vn.sun.public_service_manager.entity.Application> applications = applicationRepository
                        .findAll(spec);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                for (vn.sun.public_service_manager.entity.Application app : applications) {
                    // Lấy trạng thái mới nhất từ danh sách statuses
                    String currentStatus = "N/A";
                    if (app.getStatuses() != null && !app.getStatuses().isEmpty()) {
                        // Sắp xếp hoặc lấy phần tử cuối cùng (giả định phần tử cuối là mới nhất)
                        ApplicationStatus lastStatus = app.getStatuses().get(app.getStatuses().size() - 1);
                        currentStatus = lastStatus.getStatus() != null ? lastStatus.getStatus().name() : "PENDING";
                    }

                    String appCode = app.getApplicationCode() != null ? app.getApplicationCode() : "";
                    String serviceName = (app.getService() != null) ? app.getService().getName() : "N/A";
                    String citizenName = (app.getCitizen() != null) ? app.getCitizen().getFullName() : "N/A";
                    String submittedAt = (app.getSubmittedAt() != null) ? app.getSubmittedAt().format(formatter) : "";
                    String note = app.getNote() != null ? app.getNote() : "";
                    String procTime = (app.getService() != null) ? String.valueOf(app.getService().getProcessingTime())
                            : "0";

                    // Ghi dòng dữ liệu vào CSV
                    csvWriter.writeNext(new String[] {
                            appCode,
                            serviceName,
                            citizenName,
                            currentStatus,
                            submittedAt,
                            note,
                            procTime
                    });
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xuất CSV Application: " + e.getMessage(), e);
        }
    }

    @Override
    public void exportCitizensToCsv(Writer writer) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            // 1. Ghi ký tự BOM ngay lập tức để đảm bảo Excel mở file UTF-8 không lỗi font
            writer.write('\ufeff');

            // 2. Khởi tạo CSVWriter sau khi đã ghi BOM
            try (CSVWriter csvWriter = new CSVWriter(writer)) {
                String[] header = { "ID", "Full Name", "DOB", "Gender", "National ID", "Phone", "Email",
                        "Total Applications" };
                csvWriter.writeNext(header);

                // 3. Lấy dữ liệu (Nếu dữ liệu cực lớn, nên dùng Pageable hoặc Stream)
                List<Citizen> citizens = citizenRepository.findAll();

                for (Citizen c : citizens) {
                    // Kiểm tra null cho từng trường để tránh NullPointerException
                    String dobStr = (c.getDob() != null) ? dateFormat.format(c.getDob()) : "";
                    String genderStr = (c.getGender() != null) ? c.getGender().name() : "";

                    // Đếm số lượng hồ sơ
                    // Lưu ý: getApplications() phải trả về List/Collection để dùng .size()
                    String totalApps = "0";
                    try {
                        if (c.getApplications() != null) {
                            totalApps = String.valueOf(c.getApplications().size());
                        }
                    } catch (Exception e) {
                        // Tránh lỗi LazyInitializationException nếu session đã đóng
                        totalApps = "N/A";
                    }

                    csvWriter.writeNext(new String[] {
                            String.valueOf(c.getId()),
                            c.getFullName() != null ? c.getFullName() : "",
                            dobStr,
                            genderStr,
                            c.getNationalId() != null ? c.getNationalId() : "",
                            c.getPhone() != null ? c.getPhone() : "",
                            c.getEmail() != null ? c.getEmail() : "",
                            totalApps
                    });
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error exporting Citizens to CSV: " + e.getMessage(), e);
        }
    }

    @Override
    public void exportApplicationsToCsv(Writer writer) {

    }

    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}
