package com.relyon.financiallife.controller;

import com.relyon.financiallife.controller.params.BaseSort;
import com.relyon.financiallife.controller.params.Pagination;
import com.relyon.financiallife.controller.params.user.UserFilters;
import com.relyon.financiallife.mapper.UserMapper;
import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.role.dto.RoleResponse;
import com.relyon.financiallife.model.user.User;
import com.relyon.financiallife.model.user.dto.request.CreateUserRequest;
import com.relyon.financiallife.model.user.dto.request.UpdateUserRequest;
import com.relyon.financiallife.model.user.dto.response.UserResponse;
import com.relyon.financiallife.service.RoleService;
import com.relyon.financiallife.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserController userController;

    @Test
    void createUsers_WithValidRequest_ShouldReturn201() {
        ArrayList<Role> roles = new ArrayList<>();
        CreateUserRequest userRequest = createUserRequest();
        User users = createUser();
        UserResponse userResponse = createUserResponse();

        when(roleService.getAllRolesByIds(userRequest.getRoles())).thenReturn(new ArrayList<>());
        when(userMapper.createUserRequestToUserModel(userRequest, roles)).thenReturn(users);
        when(userService.createUser(users)).thenReturn(users);
        when(userMapper.userToUserResponse(users)).thenReturn(userResponse);

        ResponseEntity<UserResponse> result = userController.createUser(userRequest);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(userResponse, result.getBody());
        verify(userMapper, times(1)).createUserRequestToUserModel(userRequest, new ArrayList<>());
        verify(userMapper, times(1)).userToUserResponse(users);
        verify(userService, times(1)).createUser(users);
    }

    @Test
    void getAllUsers_ShouldReturn200() {
        Pagination pagination = new Pagination();
        UserFilters userFilters = new UserFilters();
        BaseSort baseSort = new BaseSort();
        Page<User> mockedPage = new PageImpl<>(Arrays.asList(new User(), new User()));

        when(userService.getAllUsers(pagination, userFilters, baseSort)).thenReturn(mockedPage);
        when(userMapper.userToUserResponse(any(User.class))).thenReturn(new UserResponse());

        ResponseEntity<Page<UserResponse>> responseEntity = userController.getAllUsers(pagination, userFilters, baseSort);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(2, Objects.requireNonNull(responseEntity.getBody()).getContent().size());
        verify(userMapper, times(2)).userToUserResponse(any(User.class));
    }

    @Test
    void getUserById_ShouldReturn200() {
        Long userId = 1L;
        User expectedUser = createUser();

        when(userService.getUserById(userId)).thenReturn(expectedUser);
        when(userMapper.userToUserResponse(expectedUser)).thenReturn(createUserResponse());

        ResponseEntity<UserResponse> actualUser = userController.getUserById(userId);

        assertEquals(HttpStatus.OK.value(), actualUser.getStatusCode().value());
        assertEquals(expectedUser.getFirstName(), Objects.requireNonNull(actualUser.getBody()).getFirstName());
        verify(userService, times(1)).getUserById(userId);
        verify(userMapper, times(1)).userToUserResponse(expectedUser);
    }

    @Test
    void updateUsers_WithValidRequest_ShouldReturn200() {
        UpdateUserRequest userRequest = updateUserRequest();
        Long userId = 1L;
        User user = createUser();
        UserResponse userResponse = createUserResponse();

        when(roleService.getAllRolesByIds(userRequest.getRoles())).thenReturn(new ArrayList<>());
        when(userMapper.updateUserRequestToUserModel(userRequest, new ArrayList<>())).thenReturn(user);
        when(userService.updateUser(userId, user)).thenReturn(user);
        when(userMapper.userToUserResponse(user)).thenReturn(userResponse);

        ResponseEntity<UserResponse> result = userController.updateUser(userId, userRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(userResponse, result.getBody());
        verify(userMapper, times(1)).updateUserRequestToUserModel(userRequest, new ArrayList<>());
        verify(userMapper, times(1)).userToUserResponse(user);
        verify(userService, times(1)).updateUser(userId, user);
    }

    @Test
    void deleteUser_ShouldReturnNoContent() {
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        ResponseEntity<Void> voidResponseEntity = userController.deleteUser(userId);

        assertEquals(HttpStatus.NO_CONTENT, voidResponseEntity.getStatusCode());
        verify(userService, times(1)).deleteUser(userId);
    }

    private CreateUserRequest createUserRequest() {
        return CreateUserRequest.builder().firstName("John").lastName("Doe").username("johndoe").dateOfBirth(LocalDate.of(1990, 1, 1))
                .cpf("123456789").cellphoneNumber("+1 555-555-5555").email("johndoe@example.com").roles(Collections.singletonList(1)).build();
    }

    private UpdateUserRequest updateUserRequest() {
        return UpdateUserRequest.builder().firstName("John").lastName("Doe").username("johndoe").dateOfBirth(LocalDate.of(1990, 1, 1))
                .cpf("123456789").cellphoneNumber("+1 555-555-5555").email("johndoe@example.com").isEnabled(true).roles(Collections.singletonList(1)).build();
    }

    private User createUser() {
        return User.builder().id(1L).firstName("John").lastName("Doe").username("johndoe").dateOfBirth(LocalDate.of(1990, 1, 1))
                .cpf("123456789").cellphoneNumber("+1 555-555-5555").email("johndoe@example.com").password("password123").enabled(true).isNonExpired(true)
                .isNonLocked(true).isCredentialsNonExpired(true).roles(Collections.singletonList(Role.builder().name("ROLE_USER").build())).build();
    }

    private static UserResponse createUserResponse() {
        return UserResponse.builder().id(1L).firstName("John").lastName("Doe").username("johndoe").email("johndoe@example.com").roles(Arrays.asList(
                RoleResponse.builder().id(1).name("ROLE_USER").build(),
                RoleResponse.builder().id(2).name("ROLE_ADMIN").build())).build();
    }
}