package vn.sun.public_service_manager.controller;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import vn.sun.public_service_manager.dto.DepartmentDTO;
import vn.sun.public_service_manager.entity.Department;
import vn.sun.public_service_manager.entity.User;
import vn.sun.public_service_manager.repository.DepartmentRepository;
import vn.sun.public_service_manager.repository.UserRespository;
import vn.sun.public_service_manager.utils.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/admin/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final UserRespository userRespository;

    @GetMapping
    @ApiMessage("Lấy danh sách phòng ban thành công")
    public ResponseEntity<Page<DepartmentDTO>> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Department> departmentPage = departmentRepository.findAll(pageable);
        
        Page<DepartmentDTO> dtoPage = departmentPage.map(dept -> DepartmentDTO.builder()
                .id(dept.getId())
                .code(dept.getCode())
                .name(dept.getName())
                .address(dept.getAddress())
                .leaderId(dept.getLeader() != null ? dept.getLeader().getId() : null)
                .leaderName(dept.getLeader() != null ? dept.getLeader().getUsername() : null)
                .build());
        
        return ResponseEntity.ok(dtoPage);
    }
    
    @GetMapping("/all")
    @ApiMessage("Lấy tất cả phòng ban")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartmentsNoPaging() {
        List<Department> allDepts = departmentRepository.findAll();
        List<DepartmentDTO> departments = allDepts.stream()
                .map(dept -> DepartmentDTO.builder()
                        .id(dept.getId())
                        .code(dept.getCode())
                        .name(dept.getName())
                        .address(dept.getAddress())
                        .leaderId(dept.getLeader() != null ? dept.getLeader().getId() : null)
                        .leaderName(dept.getLeader() != null ? dept.getLeader().getUsername() : null)
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{id}")
    @ApiMessage("Lấy thông tin phòng ban thành công")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng ban không tồn tại"));
        
        DepartmentDTO dto = DepartmentDTO.builder()
                .id(dept.getId())
                .code(dept.getCode())
                .name(dept.getName())
                .address(dept.getAddress())
                .leaderId(dept.getLeader() != null ? dept.getLeader().getId() : null)
                .leaderName(dept.getLeader() != null ? dept.getLeader().getUsername() : null)
                .build();
        
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @ApiMessage("Tạo phòng ban thành công")
    public ResponseEntity<Long> createDepartment(@RequestBody DepartmentDTO dto) {
        // Check if code exists
        if (departmentRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Mã phòng ban đã tồn tại");
        }
        
        Department dept = new Department();
        dept.setCode(dto.getCode());
        dept.setName(dto.getName());
        dept.setAddress(dto.getAddress());
        
        // Set leader if provided
        if (dto.getLeaderId() != null) {
            User leader = userRespository.findById(dto.getLeaderId())
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
            dept.setLeader(leader);
        }
        
        Department saved = departmentRepository.save(dept);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved.getId());
    }

    @PutMapping("/{id}")
    @ApiMessage("Cập nhật phòng ban thành công")
    public ResponseEntity<Void> updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartmentDTO dto
    ) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng ban không tồn tại"));
        
        // Check if code exists (exclude current)
        if (!dept.getCode().equals(dto.getCode()) && departmentRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Mã phòng ban đã tồn tại");
        }
        
        dept.setCode(dto.getCode());
        dept.setName(dto.getName());
        dept.setAddress(dto.getAddress());
        
        // Update leader
        if (dto.getLeaderId() != null) {
            User leader = userRespository.findById(dto.getLeaderId())
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
            dept.setLeader(leader);
        } else {
            dept.setLeader(null);
        }
        
        departmentRepository.save(dept);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Xóa phòng ban thành công")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phòng ban không tồn tại"));
        departmentRepository.delete(dept);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    @ApiMessage("Xuất danh sách phòng ban thành công")
    public void exportDepartmentsToCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"departments.csv\"");
        
        try (Writer writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
            // Write BOM for Excel
            writer.write('\ufeff');
            
            // Write header
            writer.write("Mã phòng ban,Tên phòng ban,Địa chỉ\n");
            
            // Get all departments
            List<Department> departments = departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "code"));
            
            // Write data
            for (Department dept : departments) {
                writer.write(String.format("%s,%s,%s\n",
                    escapeCsv(dept.getCode()),
                    escapeCsv(dept.getName()),
                    escapeCsv(dept.getAddress() != null ? dept.getAddress() : "")
                ));
            }
        }
    }

    @PostMapping("/import")
    @ApiMessage("Nhập danh sách phòng ban thành công")
    public ResponseEntity<Map<String, Object>> importDepartmentsFromCsv(
            @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            throw new RuntimeException("File không được để trống");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".csv")) {
            throw new RuntimeException("Chỉ chấp nhận file CSV");
        }
        
        Map<String, Object> result = new HashMap<>();
        int addedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;
        
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip BOM if present
                if (lineNumber == 1 && line.startsWith("\ufeff")) {
                    line = line.substring(1);
                }
                
                // Skip header
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    String[] fields = parseCsvLine(line);
                    
                    if (fields.length < 2) {
                        errorCount++;
                        continue;
                    }
                    
                    String code = fields[0].trim();
                    String name = fields[1].trim();
                    String address = fields.length > 2 ? fields[2].trim() : "";
                    
                    // Check if code already exists
                    if (departmentRepository.existsByCode(code)) {
                        skippedCount++;
                        continue;
                    }
                    
                    // Create new department
                    Department dept = new Department();
                    dept.setCode(code);
                    dept.setName(name);
                    dept.setAddress(address.isEmpty() ? null : address);
                    
                    departmentRepository.save(dept);
                    addedCount++;
                    
                } catch (Exception e) {
                    errorCount++;
                }
            }
            
            result.put("added", addedCount);
            result.put("skipped", skippedCount);
            result.put("errors", errorCount);
            result.put("message", String.format(
                "Đã thêm %d phòng ban mới, bỏ qua %d phòng ban đã tồn tại, %d lỗi",
                addedCount, skippedCount, errorCount));
            
            return ResponseEntity.ok(result);
            
        } catch (IOException e) {
            throw new RuntimeException("Đọc file thất bại: " + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String[] parseCsvLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    currentField.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString());
        
        return fields.toArray(new String[0]);
    }
}
