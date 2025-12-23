package vn.sun.public_service_manager.service;

import vn.sun.public_service_manager.entity.Citizen;
import vn.sun.public_service_manager.entity.User;
import vn.sun.public_service_manager.repository.CitizenRepository;
import vn.sun.public_service_manager.repository.UserRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CombinedUserDetailsService implements UserDetailsService {

    @Autowired
    private CitizenRepository citizenRepository;

    @Autowired
    private UserRespository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

        /* ================= USER (ADMIN / STAFF) ================= */
        Optional<User> userOpt = userRepository.findByUsername(identifier);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toList());

            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    user.getActive(), // ✅ enabled
                    true, // accountNonExpired
                    true, // credentialsNonExpired
                    true, // accountNonLocked
                    authorities);
        }

        /* ================= CITIZEN ================= */
        Optional<Citizen> citizenOpt = citizenRepository.findByNationalId(identifier);
        Citizen citizen = citizenOpt.orElseThrow(() ->
                new UsernameNotFoundException("User not found with id: " + identifier)
        );

            return org.springframework.security.core.userdetails.User
                    .withUsername(citizen.getNationalId())
                    .password(citizen.getPassword())
                    .authorities("ROLE_CITIZEN")
                    .accountExpired(false).accountLocked(false).credentialsExpired(false).disabled(!citizen.isActive())
                    .build();
    }
}
