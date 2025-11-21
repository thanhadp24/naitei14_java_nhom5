package vn.sun.public_service_manager.service;

import vn.sun.public_service_manager.dto.ServiceTypeDTO;
import vn.sun.public_service_manager.dto.ServiceTypePageResponse;

public interface ServiceTypeService {
    
    ServiceTypePageResponse getAllServiceTypes(int page, int size, String sortBy, String sortDir);
    
    ServiceTypePageResponse searchByName(String name, int page, int size);
    
    ServiceTypeDTO getServiceTypeById(Long id);
}
