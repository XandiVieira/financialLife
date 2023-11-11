package com.relyon.financiallife.service;

import com.relyon.financiallife.controller.params.BaseSort;
import com.relyon.financiallife.controller.params.Pagination;
import com.relyon.financiallife.controller.params.user.RolesData;
import com.relyon.financiallife.controller.params.user.UserFilters;
import com.relyon.financiallife.exception.custom.AuthenticationFailedException;
import com.relyon.financiallife.exception.custom.ForbiddenException;
import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.user.User;
import com.relyon.financiallife.model.user.UserExtras;
import com.relyon.financiallife.repository.PasswordHistoryRepository;
import com.relyon.financiallife.repository.UserRepository;
import com.relyon.financiallife.repository.specification.UserSpecification;
import com.relyon.financiallife.utils.RandomPasswordGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.webjars.NotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordHistoryRepository passwordHistoryRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private RandomPasswordGenerator randomPasswordGenerator;
    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private UserService userService;

    @Test
    void createUsers_WithValidRequest_ShouldReturnCreated() {
        User usersRequest = createUser();
        User userResponse = createUserResponse();

        when(randomPasswordGenerator.generateRandomPassword(anyInt())).thenReturn(usersRequest.getPassword());
        when(passwordEncoder.encode(usersRequest.getPassword())).thenReturn("$2a$10$CYJAhgUwR6J1PYcwMfzAJevj77Bh3JQgC6hdN8KjLKz6nRZ6F1JLW");
        when(userRepository.save(usersRequest)).thenReturn(userResponse);

        User response = userService.createUser(usersRequest);

        assertEquals(userResponse, response);
        verify(userRepository, times(1)).save(usersRequest);
    }

    @Test
    void createAdminUsers_AsNonAdmin_WithValidRequest_ShouldThrowForbiddenException() {
        User usersRequest = createUser();
        usersRequest.getRoles().get(0).setName("ROLE_ADMIN");

        setAuthenticationWithUser("ROLE_MANAGER");

        assertThrows(ForbiddenException.class, () -> userService.createUser(usersRequest));
    }

    @Test
    void updateLoginAttempts_WithExistingUser_ShouldReturnUpdatedUser() {
        User usersRequest = createUser();

        when(userRepository.findById(usersRequest.getId())).thenReturn(Optional.of(usersRequest));
        userService.updateLoginAttempts(usersRequest);

        verify(userRepository, times(1)).save(usersRequest);
    }

    @Test
    void updateLoginAttempts_WithNonExistingUser_ShouldThrowNotFoundException() {
        User usersRequest = createUser();

        when(userRepository.findByIdWithoutAdmins(usersRequest.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(usersRequest.getId()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getAllUsers_WithAdminUser_ShouldReturnListOfAllUsers() {
        List<User> users = Collections.singletonList(createUser());
        Page<User> page = new PageImpl<>(users);
        Pagination pagination = new Pagination(0, 10);
        UserFilters userFilters = new UserFilters();
        BaseSort baseSort = new BaseSort("createdBy,-name");

        setAuthenticationWithUser("ROLE_ADMIN");

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<User> userPage = userService.getAllUsers(pagination, userFilters, baseSort);

        assertEquals(1, userPage.getContent().size());
        assertEquals("John", userPage.getContent().get(0).getFirstName());
        verify(userRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllUsers_AsNonAdminUser_ShouldReturnListOfNonAdminUsers() {
        List<User> users = Collections.singletonList(createUser());
        Page<User> page = new PageImpl<>(users);
        Pagination pagination = new Pagination(0, 10);
        UserFilters userFilters = new UserFilters();
        BaseSort baseSort = new BaseSort("createdBy,-name");

        setAuthenticationWithUser("ROLE_MANAGER");

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<User> userPage = userService.getAllUsers(pagination, userFilters, baseSort);

        assertEquals(1, userPage.getContent().size());
        assertEquals("John", userPage.getContent().get(0).getFirstName());
        verify(userRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllUsers_WithRoleInSpecification_ShouldReturnListOfUsers() {
        List<User> users = Collections.singletonList(createUser());
        Page<User> page = new PageImpl<>(users);
        Pagination pagination = new Pagination(0, 10);
        UserFilters userFilters = new UserFilters();
        userFilters.setRolesData(new RolesData("admin", false));
        BaseSort baseSort = new BaseSort("createdBy,-name");
        Specification<User> userSpecification = buildUserSpecification(userFilters);
        Pageable pageable = buildPagination(pagination, baseSort);

        setAuthenticationWithUser("ROLE_ADMIN");
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<User> userPage = userService.getAllUsers(pagination, userFilters, baseSort);

        assertEquals(1, userPage.getContent().size());
        assertEquals("John", userPage.getContent().get(0).getFirstName());
        verify(userRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
        verify(userRepository, never()).findAll(userSpecification, pageable);
    }

    @Test
    void getAllUsers_WithNullAuthentication_ShouldThrowAuthenticationFailedException() {
        Pagination pagination = new Pagination(0, 10);
        UserFilters userFilters = new UserFilters();
        BaseSort baseSort = new BaseSort("createdBy,-name");

        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(AuthenticationFailedException.class, () -> userService.getAllUsers(pagination, userFilters, baseSort));
    }

    @Test
    void getUserById_AsAdmin_ShouldReturnUserById() {
        Long userId = 1L;
        User user = createUser();

        setAuthenticationWithUser("ROLE_ADMIN");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserById(userId);

        assertEquals(user, result);
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_AsNonAdmin_ShouldReturnUserById() {
        Long userId = 1L;
        User user = createUser();

        setAuthenticationWithUser("ROLE_MANAGER");

        when(userRepository.findByIdWithoutAdmins(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserById(userId);

        assertEquals(user, result);
        verify(userRepository).findByIdWithoutAdmins(userId);
    }

    @Test
    void getUserById_AsNonAdmin_WithNonExistingId_ShouldThrowNotFoundException() {
        Long userId = 1L;

        setAuthenticationWithUser("ROLE_MANAGER");
        when(userRepository.findByIdWithoutAdmins(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void getUserByEmail_WithExistingEmail_ShouldReturnUserByEmail() {
        User user = createUser();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail(user.getEmail());

        assertEquals(user, result);
        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void getUserByEmail_WithNonExistingEmail_ShouldThrowNotFoundException() {
        User user = createUser();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserByEmail(user.getEmail()));
        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void updateLastLogin_ShouldSetLastLoginDate() {
        User existingUser = createUser();

        when(userService.updateLastLogin(existingUser)).thenReturn(existingUser);

        userService.updateLastLogin(existingUser);

        assertNotNull(existingUser.getUserExtras().getLastLogin());
        verify(userRepository).save(existingUser);
    }

    @Test
    void getUserById_AsAdmin_WithNonExistentIds_ShouldThrowNotFoundException() {
        Long userId = 1L;

        setAuthenticationWithUser("ROLE_ADMIN");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void updateUser_WithValidRequest_ShouldReturnUpdatedUser() {
        Long id = 1L;
        User userRequest = createUser();
        User existingUser = createUser();

        setAuthenticationWithUser("ROLE_ADMIN");
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(userRequest);

        User updatedUser = userService.updateUser(id, userRequest);

        assertEquals(userRequest.getFirstName(), updatedUser.getFirstName());
        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).save(userRequest);
    }

    @Test
    void updateUser_WithAdminUser_AsOnlyAdminUser_ShouldThrowForbiddenException(){
        User existingUser = createUser();
        List<Role> roles = new ArrayList<>();
        roles.add(existingUser.getRoles().get(0));
        roles.add(new Role(1, "ROLE_ADMIN"));
        existingUser.setRoles(roles);
        setAuthenticationWithUser("ROLE_ADMIN");
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.countUsersByRolesName("ROLE_ADMIN")).thenReturn(1);

        User user = createUser();

        assertThrows(ForbiddenException.class, () -> userService.updateUser(user.getId(), user));
    }

    @Test
    void deleteUser_WithExistentId_ShouldDeleteUserSuccessfully() {
        Long userId = 1L;
        User user = createUser();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUser_AsNonAdminRole_WithExistentId_ShouldSetUserEnabledToFalse() {
        Long userId = 1L;
        User user = createUser();

        when(userRepository.findByIdWithoutAdmins(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository, times(1)).save(user);
        verify(userRepository, never()).delete(user);
    }

    @Test
    void deleteUser_WithNonExistentId_ShouldThrowNotFoundException() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void deleteUser_BeingOnlyUserWithAdminRole_ShouldThrowForbiddenException() {
        Long userId = 1L;
        String roleAdmin = "ROLE_ADMIN";
        User user = createUser();
        user.getRoles().get(0).setName(roleAdmin);

        when(userRepository.countUsersByRolesName(roleAdmin)).thenReturn(1);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ForbiddenException.class, () -> userService.deleteUser(userId));
        verify(userRepository, never()).deleteById(any());
    }

    private void setAuthenticationWithUser(String ROLE_ADMIN) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(ROLE_ADMIN));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        doReturn(authorities).when(authentication).getAuthorities();
    }

    private static PageRequest buildPagination(Pagination pagination, BaseSort baseSort) {
        return PageRequest.of(
                pagination.getPageNumber(),
                pagination.getPageSize(),
                Sort.by(getSort(baseSort.getSort()))
        );
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
        UserSpecification.UserSpecificationBuilder builder = UserSpecification.builder().firstName(userFilters.getFirstName())
                .lastName(userFilters.getLastName()).username(userFilters.getUsername()).dateOfBirth(userFilters.getDateOfBirth())
                .cpf(userFilters.getCpf()).cellphoneNumber(userFilters.getCellphoneNumber()).email(userFilters.getEmail())
                .createdBy(userFilters.getCreatedBy()).lastModifiedBy(userFilters.getLastModifiedBy()).enabled(userFilters.getEnabled());

        if (userFilters.getRolesData().isRolesSearchInclusive()) {
            builder.rolesInclusive(getRoles(userFilters.getRolesData().getRoles()));
        } else {
            builder.rolesExclusive(getRoles(userFilters.getRolesData().getRoles()));
        }
        return builder.build();
    }

    private User createUser() {
        return User.builder().id(1L).firstName("John").lastName("Doe").username("johndoe").dateOfBirth(LocalDate.of(1990, 1, 1))
                .cpf("123456789").cellphoneNumber("+1 555-555-5555").email("johndoe@example.com").password("Password@123").passwordConfirmation("Password@123").enabled(true).isNonExpired(true)
                .userExtras(UserExtras.builder().loginAttempts(0).lastLogin(LocalDateTime.now()).passwordRedefinitionAttempts(0).passwordRedefinitionBlockExpirationTime(LocalDateTime.now()).build())
                .isNonLocked(true).isCredentialsNonExpired(true).roles(Collections.singletonList(Role.builder().name("ROLE_USER").build())).build();
    }

    private static User createUserResponse() {
        return User.builder().id(1L).firstName("John").lastName("Doe").username("johndoe").email("johndoe@example.com").roles(Arrays.asList(
                Role.builder().id(1).name("ROLE_USER").build(),
                Role.builder().id(2).name("ROLE_ADMIN").build())).build();
    }
}