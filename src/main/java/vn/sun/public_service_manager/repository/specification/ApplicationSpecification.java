package vn.sun.public_service_manager.repository.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import vn.sun.public_service_manager.dto.ApplicationFilterDTO;
import vn.sun.public_service_manager.entity.*;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSpecification {

    public static Specification<Application> filterApplications(ApplicationFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Join with service
            Join<Application, vn.sun.public_service_manager.entity.Service> serviceJoin = root.join("service", JoinType.LEFT);
            
            // Join with citizen
            Join<Application, Citizen> citizenJoin = root.join("citizen", JoinType.LEFT);
            
            // Join with statuses to get current status
            Join<Application, ApplicationStatus> statusJoin = root.join("statuses", JoinType.LEFT);

            // Filter by status
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(statusJoin.get("status"), filter.getStatus()));
            }

            // Filter by service type
            if (filter.getServiceTypeId() != null) {
                Join<vn.sun.public_service_manager.entity.Service, ServiceType> serviceTypeJoin = 
                    serviceJoin.join("serviceType", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(serviceTypeJoin.get("id"), filter.getServiceTypeId()));
            }

            // Filter by service
            if (filter.getServiceId() != null) {
                predicates.add(criteriaBuilder.equal(serviceJoin.get("id"), filter.getServiceId()));
            }

            // Filter by citizen national ID
            if (filter.getCitizenNationalId() != null && !filter.getCitizenNationalId().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(citizenJoin.get("nationalId")), 
                    "%" + filter.getCitizenNationalId().toLowerCase() + "%"
                ));
            }

            // Filter by citizen name
            if (filter.getCitizenName() != null && !filter.getCitizenName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(citizenJoin.get("fullName")), 
                    "%" + filter.getCitizenName().toLowerCase() + "%"
                ));
            }

            // Filter by assigned staff
            if (filter.getAssignedStaffId() != null) {
                Join<Application, User> assignedStaffJoin = root.join("assignedStaff", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(assignedStaffJoin.get("id"), filter.getAssignedStaffId()));
            }

            // Ensure distinct results
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
