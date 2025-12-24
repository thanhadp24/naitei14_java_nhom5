package vn.sun.public_service_manager.repository.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import vn.sun.public_service_manager.dto.ApplicationFilterDTO;
import vn.sun.public_service_manager.entity.*;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSpecification {

    public static Specification<Application> filterApplications(ApplicationFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only get active applications (soft delete)
            predicates.add(criteriaBuilder.equal(root.get("active"), true));

            // Join with service
            Join<Application, vn.sun.public_service_manager.entity.Service> serviceJoin = root.join("service", JoinType.LEFT);
            
            // Only get applications with active services
            predicates.add(criteriaBuilder.equal(serviceJoin.get("active"), true));
            
            // Join with citizen
            Join<Application, Citizen> citizenJoin = root.join("citizen", JoinType.LEFT);

            // Only get applications whose citizen is active
            predicates.add(criteriaBuilder.equal(citizenJoin.get("active"), true));

            // Filter by status - only filter by the LATEST status using subquery
            if (filter.getStatus() != null) {
                // Subquery to get the latest status ID for each application
                Subquery<Long> latestStatusSubquery = query.subquery(Long.class);
                jakarta.persistence.criteria.Root<ApplicationStatus> statusSubRoot = latestStatusSubquery.from(ApplicationStatus.class);
                
                latestStatusSubquery.select(criteriaBuilder.max(statusSubRoot.get("id")))
                        .where(criteriaBuilder.equal(statusSubRoot.get("application"), root));
                
                // Join with statuses and filter by latest
                Join<Application, ApplicationStatus> statusJoin = root.join("statuses", JoinType.INNER);
                predicates.add(criteriaBuilder.and(
                    criteriaBuilder.equal(statusJoin.get("status"), filter.getStatus()),
                    criteriaBuilder.in(statusJoin.get("id")).value(latestStatusSubquery)
                ));
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

            // Filter by department (cho MANAGER)
            if (filter.getDepartmentId() != null) {
                Join<vn.sun.public_service_manager.entity.Service, Department> departmentJoin = 
                    serviceJoin.join("responsibleDepartment", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(departmentJoin.get("id"), filter.getDepartmentId()));
            }

            // Ensure distinct results
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Application> filterByCriteria(ApplicationFilterDTO filter) {
        return filterApplications(filter);
    }
}
