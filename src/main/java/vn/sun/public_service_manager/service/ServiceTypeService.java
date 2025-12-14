package vn.sun.public_service_manager.service;

import vn.sun.public_service_manager.entity.ServiceType;
import vn.sun.public_service_manager.repository.ServiceTypeRepository;
import vn.sun.public_service_manager.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;
    private final ServiceRepository serviceRepository;

    public List<ServiceType> getAllServiceTypes() {
        return serviceTypeRepository.findAll();
    }

    public Optional<ServiceType> getServiceTypeById(Long id) {
        return serviceTypeRepository.findById(id);
    }
    @Transactional
    public ServiceType saveServiceType(ServiceType serviceType) {

        String category = serviceType.getCategory().trim();
        serviceType.setCategory(category);
        Optional<ServiceType> existingType = serviceTypeRepository.findByCategoryIgnoreCase(category);

        if (existingType.isPresent()) {
            Long existingId = existingType.get().getId();
            if (serviceType.getId() == null || !existingId.equals(serviceType.getId())) {
                throw new DataIntegrityViolationException("Category '" + category + "' already exists.");
            }
        }
        try {
            return serviceTypeRepository.save(serviceType);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Failed to save due to database constraint violation.");
        }
    }

    @Transactional
    public void deleteServiceType(Long id) throws IllegalArgumentException, IllegalStateException {

        if (!serviceTypeRepository.existsById(id)) {
            throw new IllegalArgumentException("ServiceType ID " + id + " not found.");
        }
        long servicesCount = serviceRepository.countByServiceTypeId(id);

        if (servicesCount > 0) {
            throw new IllegalStateException("Cannot delete ServiceType ID " + id +
                    " because it is currently linked to " + servicesCount + " service(s).");
        }

        serviceTypeRepository.deleteById(id);
    }
}
