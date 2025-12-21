package vn.sun.public_service_manager.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.sun.public_service_manager.dto.ServiceDTO;
import vn.sun.public_service_manager.dto.ServicePageResponse;
import vn.sun.public_service_manager.exception.ResourceNotFoundException;
import vn.sun.public_service_manager.repository.ServiceRepository;
import vn.sun.public_service_manager.service.ServiceService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceServiceImpl implements ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Override
    public ServicePageResponse getAllServices(int page, int size, String sortBy, String sortDir, String keyword) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page - 1, size, sort);
        if (keyword != null && !keyword.isEmpty()) {
            return mapToPageResponse(serviceRepository
                    .findByKeyword(keyword, pageable));
        }

        return mapToPageResponse(serviceRepository.findAll(pageable));
    }

    // @Override
    // public ServicePageResponse searchByKeyword(String keyword, int page, int
    // size) {
    // Pageable pageable = PageRequest.of(page, size);
    // Page<vn.sun.public_service_manager.entity.Service> servicePage =
    // serviceRepository
    // .findByKeyword(keyword, pageable);

    // return mapToPageResponse(servicePage);
    // }

    @Override
    public ServicePageResponse searchByServiceType(Long serviceTypeId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<vn.sun.public_service_manager.entity.Service> servicePage = serviceRepository
                .findByServiceTypeId(serviceTypeId, pageable);
        return mapToPageResponse(servicePage);
    }

    @Override
    public ServicePageResponse searchByServiceTypeAndKeyword(Long serviceTypeId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<vn.sun.public_service_manager.entity.Service> servicePage = serviceRepository
                .findByServiceTypeIdAndKeyword(serviceTypeId, keyword, pageable);
        return mapToPageResponse(servicePage);
    }

    @Override
    public ServiceDTO getServiceById(Long id) {
        vn.sun.public_service_manager.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        return ServiceDTO.fromEntity(service);
    }

    private ServicePageResponse mapToPageResponse(Page<vn.sun.public_service_manager.entity.Service> servicePage) {
        List<ServiceDTO> serviceDTOs = servicePage.getContent().stream()
                .map(ServiceDTO::fromEntity)
                .collect(Collectors.toList());

        ServicePageResponse response = new ServicePageResponse();
        response.setContent(serviceDTOs);
        response.setCurrentPage(servicePage.getNumber() == 0 ? 1 : servicePage.getNumber());
        response.setTotalPages(servicePage.getTotalPages());
        response.setTotalElements(servicePage.getTotalElements());
        response.setSize(servicePage.getSize());
        response.setHasNext(servicePage.hasNext());
        response.setHasPrevious(servicePage.hasPrevious());

        return response;
    }
}
