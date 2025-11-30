package vn.sun.public_service_manager.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.entity.User;
import vn.sun.public_service_manager.exception.ResourceNotFoundException;
import vn.sun.public_service_manager.repository.CitizenRepository;

@Service
public class SecurityUtil {

    private final CitizenRepository citizenRepository;

    public SecurityUtil(CitizenRepository citizenRepository) {
        this.citizenRepository = citizenRepository;
    }

    public static String getCurrentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public Citizen getCurrentUserLogin() {
        String nationalId = SecurityContextHolder.getContext().getAuthentication().getName();
        return citizenRepository.findByNationalId(nationalId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen not found with national ID: " + nationalId));
    }

}
