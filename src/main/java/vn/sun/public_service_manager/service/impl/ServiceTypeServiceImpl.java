package vn.sun.public_service_manager.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.sun.public_service_manager.entity.ServiceType;
import vn.sun.public_service_manager.repository.ServiceTypeRepository;
import vn.sun.public_service_manager.service.ServiceTypeService;

@Service
@RequiredArgsConstructor
public class ServiceTypeServiceImpl implements ServiceTypeService {
    
    private final ServiceTypeRepository serviceTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ServiceType> getAllServiceTypes() {
        return serviceTypeRepository.findAll();
    }
}
