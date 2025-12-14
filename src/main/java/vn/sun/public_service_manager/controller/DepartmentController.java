package vn.sun.public_service_manager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.sun.public_service_manager.dto.DepartmentDTO;
import vn.sun.public_service_manager.entity.Department;
import vn.sun.public_service_manager.repository.DepartmentRepository;
import vn.sun.public_service_manager.utils.annotation.ApiMessage;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

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
}
