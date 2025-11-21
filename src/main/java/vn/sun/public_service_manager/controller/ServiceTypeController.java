package vn.sun.public_service_manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.sun.public_service_manager.dto.ServiceTypeDTO;
import vn.sun.public_service_manager.dto.ServiceTypePageResponse;
import vn.sun.public_service_manager.service.ServiceTypeService;

@RestController
@RequestMapping("/api/service-types")
public class ServiceTypeController {
    
    @Autowired
    private ServiceTypeService serviceTypeService;
    
    @GetMapping
    public ResponseEntity<ServiceTypePageResponse> getAllServiceTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        ServiceTypePageResponse response = serviceTypeService.getAllServiceTypes(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    public ResponseEntity<ServiceTypePageResponse> searchServiceTypes(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        ServiceTypePageResponse response = serviceTypeService.searchByName(name, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getServiceTypeById(@PathVariable Long id) {
        try {
            ServiceTypeDTO serviceTypeDTO = serviceTypeService.getServiceTypeById(id);
            return ResponseEntity.ok(serviceTypeDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
        }
    }
}
