package com.relyon.financiallife.configuration.security;

import com.relyon.financiallife.model.permissions.Permission;
import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.user.User;
import com.relyon.financiallife.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        log.info("Loading user details by email: {}", email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), true, true, true, true, getAuthorities(user.getRoles()));
        log.info("User details loaded successfully for email: {}", email);
        return userDetails;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Collection<Role> roles) {
        return getGrantedAuthorities(getPermissions(roles));
    }

    private List<String> getPermissions(Collection<Role> roles) {
        return roles.stream()
                .flatMap(role -> Stream.concat(
                        Stream.of(role.getName()),
                        role.getPermissions().stream().map(Permission::getName))).toList();
    }

    private List<SimpleGrantedAuthority> getGrantedAuthorities(List<String> permissions) {
        return permissions.stream()
                .map(SimpleGrantedAuthority::new).toList();
    }
}