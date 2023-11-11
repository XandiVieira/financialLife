package com.relyon.financiallife.service;

import com.relyon.financiallife.controller.params.BaseSort;
import com.relyon.financiallife.controller.params.Pagination;
import com.relyon.financiallife.controller.params.user.UserFilters;
import com.relyon.financiallife.exception.custom.AuthenticationFailedException;
import com.relyon.financiallife.exception.custom.ForbiddenException;
import com.relyon.financiallife.model.password.PasswordHistory;
import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.user.User;
import com.relyon.financiallife.repository.PasswordHistoryRepository;
import com.relyon.financiallife.repository.UserRepository;
import com.relyon.financiallife.repository.specification.UserSpecification;
import com.relyon.financiallife.utils.PasswordValidator;
import com.relyon.financiallife.utils.RandomPasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.relyon.financiallife.utils.Utils.isManager;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final RandomPasswordGenerator randomPasswordGenerator;
    private final PasswordResetService passwordResetService;

    @Transactional
    public User createUser(User userRequest) {
        log.info("Creating user: {}", userRequest.getEmail());
        checkForbiddenRole(userRequest);

        setRandomGeneratedPassword(userRequest);

        String password = userRequest.getPassword();
        PasswordValidator.validate(password, userRequest.getPasswordConfirmation(), userRequest);
        userRequest.setPassword(passwordEncoder.encode(password));

        User savedUser = userRepository.save(userRequest);
        log.info("User created successfully with id: {}", savedUser.getId());

        passwordHistoryRepository.save(new PasswordHistory(savedUser, savedUser.getPassword()));
        log.info("PasswordHistory updated successfully for the user: {}", savedUser.getEmail());

        CompletableFuture.runAsync(() -> passwordResetService.sendWelcomeEmail(userRequest.getEmail(), password));

        return savedUser;
    }

    private void setRandomGeneratedPassword(User userRequest) {
        log.info("Generating random password...");
        String randomGeneratedPassword = randomPasswordGenerator.generateRandomPassword(16);
        userRequest.setPassword(randomGeneratedPassword);
        userRequest.setPasswordConfirmation(randomGeneratedPassword);
    }

    public Page<User> getAllUsers(Pagination pagination, UserFilters userFilters, BaseSort userSort) {
        log.info("Getting all users");

        Specification<User> userSpecification = buildUserSpecification(userFilters);
        Pageable pageable = PageRequest.of(pagination.getPageNumber(), pagination.getPageSize(), Sort.by(getSort(userSort.getSort())));

        Page<User> usersPage = userRepository.findAll(userSpecification, pageable);
        if (!isAdmin()) {
            usersPage = new PageImpl<>(usersPage.getContent().stream().filter(user -> !user.getRoles().stream().map(Role::getName).toList().contains("ROLE_ADMIN")).toList());
        }

        log.info("Retrieved {} users.", usersPage.getTotalElements());
        return usersPage;
    }

    public User getUserById(Long id) {
        log.info("Getting user with id: {}", id);
        User user;
        if (isAdmin()) {
            user = getUser(id);
        } else {
            user = getUserExcludeAdmin(id);
        }
        log.info("Retrieved user with id: {}", id);
        return user;
    }

    public User getUserByEmail(String email) {
        log.info("Getting user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User with email: {} not found", email);
                    return new NotFoundException("User with email " + email + " was not found.");
                });

        log.info("Retrieved user with email: {}", email);
        return user;
    }

    @Transactional
    public User updateUser(Long id, User userRequest) {
        log.info("Updating user with id: {}, user: {}", id, userRequest.getEmail());
        checkForbiddenRole(userRequest);

        User existingUser = isAdmin() ? getUser(id) : getUserExcludeAdmin(id);
        userRequest.setId(existingUser.getId());
        userRequest.setPassword(existingUser.getPassword());

        checkDefaultRolesHasBeenRemoved(id, userRequest, existingUser);

        User updatedUser = userRepository.save(userRequest);
        log.info("User with id: {} updated successfully", id);
        return updatedUser;
    }

    private void checkDefaultRolesHasBeenRemoved(Long id, User userRequest, User existingUser) {
        String roleAdmin = "ROLE_ADMIN";
        String roleManager = "ROLE_MANAGER";
        if (isOnlyUserWithRole(id, existingUser)) {
            List<String> existingRoles = existingUser.getRoles().stream().map(Role::getName).toList();
            List<String> requestRoles = userRequest.getRoles().stream().map(Role::getName).toList();

            boolean hasAdminRoleChanged = existingRoles.contains(roleAdmin) && !requestRoles.contains(roleAdmin);
            boolean hasManagerRoleChanged = existingRoles.contains(roleManager) && !requestRoles.contains(roleManager);

            if ((hasAdminRoleChanged || hasManagerRoleChanged)) {
                throw new ForbiddenException("Cannot update user role as they are the only user with the 'admin' or 'manager' role");
            }
        }
    }

    public void updateLoginAttempts(User user) {
        getUser(user.getId());
        userRepository.save(user);
        log.info("User login attempts was updated.");
    }

    public User updateLastLogin(User user) {
        LocalDateTime now = LocalDateTime.now();
        user.getUserExtras().setLastLogin(now);
        log.info("User last login date/time updated to: {}", now);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        User user = isAdmin() ? getUser(id) : getUserExcludeAdmin(id);
        if (isOnlyUserWithRole(id, user)) {
            throw new ForbiddenException("Cannot delete user as they are the only user with the 'admin' or 'manager' role");
        }
        if (isAdmin()) {
            passwordHistoryRepository.deleteByUserId(user.getId());
            userRepository.delete(user);
        } else {
            user.setEnabled(false);
            userRepository.save(user);
        }
        log.info("User deleted successfully with id: {}", id);
    }

    private boolean isOnlyUserWithRole(Long id, User user) {
        if ((user.getRoles().stream().map(Role::getName).toList().contains("ROLE_ADMIN") && userRepository.countUsersByRolesName("ROLE_ADMIN") == 1) ||
                (isManager(user) && userRepository.countUsersByRolesName("ROLE_MANAGER") == 1)) {
            log.warn("Cannot delete user with id: {} as they are the only user with the 'admin' or 'manager' role", id);
            return true;
        }
        return false;
    }

    private List<String> getRoles(String roles) {
        if (StringUtils.hasText(roles)) {
            return Arrays.stream(roles.split(","))
                    .map(role -> "ROLE_" + role.trim().toUpperCase())
                    .toList();
        }
        return Collections.emptyList();
    }

    private static List<Sort.Order> getSort(String sort) {
        List<Sort.Order> orders = new ArrayList<>();
        for (String field : sort.split(",")) {
            if (field.startsWith("-")) {
                orders.add(new Sort.Order(Sort.Direction.DESC, field.substring(1)));
            } else {
                orders.add(new Sort.Order(Sort.Direction.ASC, field));
            }
        }
        return orders;
    }

    private Specification<User> buildUserSpecification(UserFilters userFilters) {
        UserSpecification.UserSpecificationBuilder builder = UserSpecification.builder()
                .firstName(userFilters.getFirstName())
                .lastName(userFilters.getLastName())
                .username(userFilters.getUsername())
                .dateOfBirth(userFilters.getDateOfBirth())
                .cpf(userFilters.getCpf())
                .cellphoneNumber(userFilters.getCellphoneNumber())
                .email(userFilters.getEmail())
                .createdBy(userFilters.getCreatedBy())
                .lastModifiedBy(userFilters.getLastModifiedBy())
                .enabled(userFilters.getEnabled());

        if (userFilters.getRolesData().isRolesSearchInclusive()) {
            builder.rolesInclusive(getRoles(userFilters.getRolesData().getRoles()));
        } else {
            builder.rolesExclusive(getRoles(userFilters.getRolesData().getRoles()));
        }
        return builder.build();
    }

    private User getUser(Long id) {
        log.info("Getting user with id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with id: {} not found", id);
                    return new NotFoundException("User with id " + id + " was not found.");
                });
    }

    private User getUserExcludeAdmin(Long id) {
        log.info("Getting user (excluding admin) with id: {}", id);
        return userRepository.findByIdWithoutAdmins(id)
                .orElseThrow(() -> {
                    log.error("User with id: {} not found", id);
                    return new NotFoundException("User with id " + id + " was not found.");
                });
    }

    private void checkForbiddenRole(User user) {
        if (!isAdmin() && (user.getRoles().stream().map(Role::getName).toList().contains("ROLE_ADMIN"))) {
            throw new ForbiddenException("User is not allowed to create or update a user with this level of permission");
        }
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AuthenticationFailedException("User not authenticated.");
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList()
                .contains("ROLE_ADMIN");
    }
}