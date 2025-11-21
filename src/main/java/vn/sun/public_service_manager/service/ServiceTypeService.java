package vn.sun.public_service_manager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.sun.public_service_manager.dto.ServiceTypeDTO;
import vn.sun.public_service_manager.dto.ServiceTypePageResponse;
import vn.sun.public_service_manager.entity.ServiceType;
import vn.sun.public_service_manager.repository.ServiceTypeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceTypeService {
    
    @Autowired
    private ServiceTypeRepository serviceTypeRepository;
    
    public ServiceTypePageResponse getAllServiceTypes(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ServiceType> serviceTypePage = serviceTypeRepository.findAll(pageable);
        
        return mapToPageResponse(serviceTypePage);
    }
    
    public ServiceTypePageResponse searchByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ServiceType> serviceTypePage = serviceTypeRepository.findByNameContainingIgnoreCase(name, pageable);
        
        return mapToPageResponse(serviceTypePage);
    }
    
    public ServiceTypeDTO getServiceTypeById(Long id) {
        ServiceType serviceType = serviceTypeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Service type not found with id: " + id));
        
        return ServiceTypeDTO.fromEntity(serviceType);
    }
    
    private ServiceTypePageResponse mapToPageResponse(Page<ServiceType> serviceTypePage) {
        List<ServiceTypeDTO> serviceTypeDTOs = serviceTypePage.getContent().stream()
            .map(ServiceTypeDTO::fromEntity)
            .collect(Collectors.toList());
        
        ServiceTypePageResponse response = new ServiceTypePageResponse();
        response.setContent(serviceTypeDTOs);
        response.setCurrentPage(serviceTypePage.getNumber());
        response.setTotalPages(serviceTypePage.getTotalPages());
        response.setTotalElements(serviceTypePage.getTotalElements());
        response.setSize(serviceTypePage.getSize());
        response.setHasNext(serviceTypePage.hasNext());
        response.setHasPrevious(serviceTypePage.hasPrevious());
        
        return response;
    }
}
