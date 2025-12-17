package vn.sun.public_service_manager.service;

import vn.sun.public_service_manager.dto.ServiceDTO;
import vn.sun.public_service_manager.dto.ServicePageResponse;

public interface ServiceService {

    ServicePageResponse getAllServices(int page, int size, String sortBy, String sortDir, String keyword);

    // ServicePageResponse searchByKeyword(String name, int page, int size);

    ServicePageResponse searchByServiceType(Long serviceTypeId, int page, int size);

    ServicePageResponse searchByServiceTypeAndKeyword(Long serviceTypeId, String keyword, int page, int size);

    ServiceDTO getServiceById(Long id);
}
