package vn.sun.public_service_manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.sun.public_service_manager.entity.User;
import vn.sun.public_service_manager.entity.Role;
import java.util.Set;

@Repository
public interface UserRespository extends JpaRepository<User, Long> {
        Optional<User> findByEmail(String email);

        Optional<User> findByUsername(String username);

        @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles LEFT JOIN FETCH u.department WHERE u.id = :id")
        Optional<User> findByIdWithRolesAndDepartment(@Param("id") Long id);

        @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH u.department d " +
                        "WHERE (:search IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        "OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:active IS NULL OR u.active = :active) " +
                        "AND (:role IS NULL OR r.name = :role) " +
                        "AND (:departmentId IS NULL OR d.id = :departmentId)")
        Page<User> findWithFilters(
                        @Param("search") String search,
                        @Param("active") Boolean active,
                        @Param("role") String role,
                        @Param("departmentId") Long departmentId,
                        Pageable pageable);

        List<User> findByRoles(Set<Role> roles);

        boolean existsByEmail(String email);

}
