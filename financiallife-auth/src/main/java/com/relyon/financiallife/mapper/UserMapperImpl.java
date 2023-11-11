package com.relyon.financiallife.mapper;

import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.role.dto.RoleResponse;
import com.relyon.financiallife.model.user.User;
import com.relyon.financiallife.model.user.UserExtras;
import com.relyon.financiallife.model.user.dto.request.CreateUserRequest;
import com.relyon.financiallife.model.user.dto.request.UpdateUserRequest;
import com.relyon.financiallife.model.user.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserMapperImpl implements UserMapper {

    private final RoleMapper roleMapper;

    @Override
    public User createUserRequestToUserModel(CreateUserRequest createUserRequest, List<Role> roles) {
        User user = User.builder()
                .firstName(createUserRequest.getFirstName())
                .lastName(createUserRequest.getLastName())
                .username(createUserRequest.getUsername())
                .dateOfBirth(createUserRequest.getDateOfBirth())
                .cpf(createUserRequest.getCpf())
                .cellphoneNumber(createUserRequest.getCellphoneNumber())
                .email(createUserRequest.getEmail())
                .enabled(false)
                .isCredentialsNonExpired(true)
                .isNonLocked(true)
                .isNonExpired(true)
                .roles(roles)
                .build();
        user.setUserExtras(UserExtras.builder().user(user).loginAttempts(0).passwordRedefinitionAttempts(0).passwordRedefinitionBlockExpirationTime(LocalDateTime.now()).passwordRedefinitionBlockExpirationTime(LocalDateTime.now()).build());
        return user;
    }

    @Override
    public User updateUserRequestToUserModel(UpdateUserRequest userRequest, List<Role> roles) {
        return User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .username(userRequest.getUsername())
                .dateOfBirth(userRequest.getDateOfBirth())
                .cpf(userRequest.getCpf())
                .cellphoneNumber(userRequest.getCellphoneNumber())
                .email(userRequest.getEmail())
                .enabled(userRequest.isEnabled())
                .isCredentialsNonExpired(true)
                .isNonLocked(true)
                .isNonExpired(true)
                .roles(roles)
                .build();
    }

    @Override
    public UserResponse userToUserResponse(User user) {
        List<RoleResponse> roleResponses = new ArrayList<>();
        if (user.getRoles() != null) {
            roleResponses = user.getRoles().stream().map(roleMapper::roleToRoleResponse).toList();
        }
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .dateOfBirth(user.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .cpf(user.getCpf())
                .cellphoneNumber(user.getCellphoneNumber())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .roles(roleResponses)
                .lastLogin(user.getUserExtras() != null && user.getUserExtras().getLastLogin() != null ? user.getUserExtras().getLastLogin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss")) : null)
                .build();
    }
}