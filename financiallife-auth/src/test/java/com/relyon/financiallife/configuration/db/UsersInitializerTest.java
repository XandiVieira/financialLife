package com.relyon.financiallife.configuration.db;

import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.user.User;
import com.relyon.financiallife.repository.PasswordHistoryRepository;
import com.relyon.financiallife.repository.RoleRepository;
import com.relyon.financiallife.repository.UserExtrasRepository;
import com.relyon.financiallife.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UsersInitializerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordHistoryRepository passwordHistoryRepository;
    @Mock
    private UserExtrasRepository userExtrasRepository;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UsersInitializer usersInitializer;

    @Test
    void run_ShouldCreateSuperUser() {
        try {
            Field field = UsersInitializer.class.getDeclaredField("initialUserPassword");
            field.setAccessible(true);
            field.set(usersInitializer, "myPassword");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        User user = buildUser();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findAllByNameIn(List.of("ROLE_ADMIN"))).thenReturn(buildRoles().stream().filter(role -> role.getId() != 3).toList());
        when(passwordEncoder.encode("myPassword")).thenReturn("$2a$10$KVLZAG1IYNX6qIjYg43gMuVHx2buDnsMQY/mL7AmXTdePBx5PLWUm");
        when(userRepository.save(any(User.class))).thenReturn(user);

        usersInitializer.run();

        verify(userRepository).findByEmail("alexandre.vieira@relyon.dev.br");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void run_WitExistingUser_ShouldNotCreateUser() {
        User existingUser = User.builder()
                .email("alexandre.vieira@relyon.dev.br")
                .build();

        when(userRepository.findByEmail(existingUser.getEmail())).thenReturn(Optional.of(existingUser));

        usersInitializer.run();

        verify(userRepository, never()).save(any(User.class));
    }

    private List<Role> buildRoles() {
        return Stream.of(
                new Role(1, "admin"),
                new Role(2, "manager"),
                new Role(3, "user")).toList();
    }

    private User buildUser() {
        return User.builder()
                .firstName("Alexandre")
                .lastName("Vieira")
                .username("alexandre.vieira")
                .dateOfBirth(LocalDate.of(1997, 6, 30))
                .cpf("012.164.370-09")
                .cellphoneNumber("(51) 99751-3229")
                .email("alexandre.vieira@relyon.dev.br")
                .password("$2a$10$KVLZAG1IYNX6qIjYg43gMuVHx2buDnsMQY/mL7AmXTdePBx5PLWUm")
                .roles(buildRoles().stream().filter(role -> role.getId() != 3).toList())
                .build();
    }
}