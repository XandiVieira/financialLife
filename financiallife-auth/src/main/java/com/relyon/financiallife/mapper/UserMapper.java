package com.relyon.financiallife.mapper;

import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.user.User;
import com.relyon.financiallife.model.user.dto.request.CreateUserRequest;
import com.relyon.financiallife.model.user.dto.request.UpdateUserRequest;
import com.relyon.financiallife.model.user.dto.response.UserResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    User createUserRequestToUserModel(CreateUserRequest createUserRequest, List<Role> roles);

    UserResponse userToUserResponse(User user);

    User updateUserRequestToUserModel(UpdateUserRequest userRequest, List<Role> roles);
}