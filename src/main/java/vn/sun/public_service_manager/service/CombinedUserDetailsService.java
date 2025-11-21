package vn.sun.public_service_manager.service;

import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.repository.CitizenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CombinedUserDetailsService implements UserDetailsService {

    @Autowired
    private CitizenRepository citizenRepository;
    // Tương lai: @Autowired private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

        // 1. THỬ TÌM KIẾM CITIZEN BẰNG NATIONAL ID
        Optional<Citizen> citizenOpt = citizenRepository.findByNationalId(identifier);

        if (citizenOpt.isPresent()) {
            Citizen citizen = citizenOpt.get();
            // Gán Role cố định cho Citizen
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_CITIZEN"));

            return new org.springframework.security.core.userdetails.User(
                    citizen.getNationalId(), // Dùng nationalId làm username của Spring Security
                    citizen.getPassword(),
                    authorities
            );
        }

        // 2. THỬ TÌM KIẾM STAFF/ADMIN (Logic sẽ thêm vào đây sau)
        // ...

        // Nếu không tìm thấy ở cả hai nơi
        throw new UsernameNotFoundException("User not found with identifier: " + identifier);
    }
}