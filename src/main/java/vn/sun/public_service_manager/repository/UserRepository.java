package vn.sun.public_service_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.sun.public_service_manager.entity.Department;
import vn.sun.public_service_manager.entity.Role;
import vn.sun.public_service_manager.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_STAFF'")
    List<User> findAllStaff();

    List<User> findByDepartmentAndRoles(Department department, List<Role> roles);
}
