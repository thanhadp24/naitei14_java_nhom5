package vn.sun.public_service_manager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    @ApiMessage("Get all departments successfully")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }
}
