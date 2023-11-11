package com.relyon.financiallife.utils;

import com.relyon.financiallife.exception.custom.PasswordValidationException;
import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.user.User;
import com.relyon.financiallife.model.user.UserExtras;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PasswordValidatorTest {

    @Test
    void validate_WithDifferentPasswords_ShouldThrowPasswordValidationException() {
        User user = createUser();
        String password = "Abc123!@#";
        String passwordConfirmation = "Abc1234!@#";

        PasswordValidationException exception = assertThrows(PasswordValidationException.class,
                () -> PasswordValidator.validate(password, passwordConfirmation, user));

        assertEquals("Password and password confirmation do not match", exception.getMessage());
    }

    @Test
    void validate_WithValidPassword_ShouldNotThrowException() {
        User user = createUser();
        String password = "Abc123!@#";

        assertDoesNotThrow(() -> PasswordValidator.validate(password, password, user));
    }

    @Test
    void validate_WithInvalidMinimumLength_ShouldThrowException() {
        User user = createUser();
        String password = "Abc1234";

        PasswordValidationException exception = assertThrows(PasswordValidationException.class,
                () -> PasswordValidator.validate(password, password, user));

        assertEquals("Password must have at least 8 characters", exception.getMessage());
    }

    @Test
    void validate_WithOnlyLetters_ShouldThrowException() {
        User user = createUser();
        String password = "abcdefghi";

        PasswordValidationException exception = assertThrows(PasswordValidationException.class,
                () -> PasswordValidator.validate(password, password, user));

        assertEquals("Password must include both letters and numbers", exception.getMessage());
    }

    @Test
    void validate_WithOnlyNumber_ShouldThrowException() {
        User user = createUser();
        String password = "123456789";

        PasswordValidationException exception = assertThrows(PasswordValidationException.class,
                () -> PasswordValidator.validate(password, password, user));

        assertEquals("Password must include both letters and numbers", exception.getMessage());
    }

    @Test
    void validate_WithSequenceOf4RepeatedChars_ShouldThrowException() {
        User user = createUser();
        String password = "7777@Ab325874";

        PasswordValidationException exception = assertThrows(PasswordValidationException.class,
                () -> PasswordValidator.validate(password, password, user));

        assertEquals("Password must not contain a sequence of more than 3 repeated characters", exception.getMessage());
    }

    @Test
    void validate_WithNoUppercase_ShouldThrowException() {
        User user = createUser();
        String password = "abc123!@#";

        PasswordValidationException exception = assertThrows(PasswordValidationException.class,
                () -> PasswordValidator.validate(password, password, user));

        assertEquals("Password must have at least one uppercase letter", exception.getMessage());
    }

    @Test
    void validate_WithNoLowercase_ShouldThrowException() {
        User user = createUser();
        String password = "ABC123!@#";

        PasswordValidationException exception = assertThrows(PasswordValidationException.class,
                () -> PasswordValidator.validate(password, password, user));

        assertEquals("Password must have at least one lowercase letter", exception.getMessage());
    }

    @Test
    void validate_WithNoSpecialCharacter_ShouldThrowException() {
        User user = createUser();
        String password = "ABC123d5";

        PasswordValidationException exception = assertThrows(PasswordValidationException.class,
                () -> PasswordValidator.validate(password, password, user));

        assertEquals("Password must have at least one special character", exception.getMessage());
    }

    @Test
    void validate_WithAscendingSequenceGreaterThan3_ShouldThrowException() {
        User user = createUser();
        String password = "Abcdfgdf1234@";

        PasswordValidationException exception = assertThrows(PasswordValidationException.class,
                () -> PasswordValidator.validate(password, password, user));

        assertEquals("Password must not have numbers in ascending or descending sequence greater than 3 characters", exception.getMessage());
    }

    @Test
    void validate_WithDescendingSequenceGreaterThan3_ShouldThrowException() {
        User user = createUser();
        String password = "Abcdfgdf9876@";

        PasswordValidationException exception = assertThrows(PasswordValidationException.class,
                () -> PasswordValidator.validate(password, password, user));

        assertEquals("Password must not have numbers in ascending or descending sequence greater than 3 characters", exception.getMessage());
    }

    @Test
    void validate_WithEmailVariation_ShouldThrowException() {
        User user = createUser();
        String password = "Abcdfgdf954564@example";

        PasswordValidationException exception = assertThrows(PasswordValidationException.class,
                () -> PasswordValidator.validate(password, password, user));

        assertTrue(exception.getMessage().contains("Password must not contain variations of user"));
    }

    @Test
    void validate_WithDateOfBirthVariation_ShouldThrowException() {
        User user = createUser();
        String password = "Abcdfgdf1990@";

        PasswordValidationException exception = assertThrows(PasswordValidationException.class,
                () -> PasswordValidator.validate(password, password, user));

        assertTrue(exception.getMessage().contains("Password must not contain variations of user"));
    }

    @Test
    void validate_WithCellphoneVariation_ShouldThrowException() {
        User user = createUser();
        String password = "Abcdfgdf7402@";

        PasswordValidationException exception = assertThrows(PasswordValidationException.class,
                () -> PasswordValidator.validate(password, password, user));

        assertTrue(exception.getMessage().contains("Password must not contain variations of user"));
    }

    @Test
    void validate_WithCPFVariation_ShouldThrowException() {
        User user = createUser();
        String password = "Abcdfgdf15910@";

        PasswordValidationException exception = assertThrows(PasswordValidationException.class,
                () -> PasswordValidator.validate(password, password, user));

        assertTrue(exception.getMessage().contains("Password must not contain variations of user"));
    }

    @Test
    void validate_WithFirstNameVariation_ShouldThrowException() {
        User user = createUser();
        String password = "Abcdfgdf159John@";

        PasswordValidationException exception = assertThrows(PasswordValidationException.class,
                () -> PasswordValidator.validate(password, password, user));

        assertTrue(exception.getMessage().contains("Password must not contain variations of user"));
    }

    @Test
    void validate_WithLastNameVariation_ShouldThrowException() {
        User user = createUser();
        String password = "Abcdfgdf159Does@";

        PasswordValidationException exception = assertThrows(PasswordValidationException.class,
                () -> PasswordValidator.validate(password, password, user));

        assertTrue(exception.getMessage().contains("Password must not contain variations of user"));
    }

    private User createUser() {
        return User.builder().id(1L).firstName("John").lastName("Doess").username("johndoe").dateOfBirth(LocalDate.of(1990, 1, 1))
                .cpf("015.357.159-11").cellphoneNumber("(51) 98740-2118").email("johndoe@example.com").password("Password@123").passwordConfirmation("Password@123").enabled(true).isNonExpired(true)
                .userExtras(UserExtras.builder().loginAttempts(0).lastLogin(LocalDateTime.now()).passwordRedefinitionAttempts(0).passwordRedefinitionBlockExpirationTime(LocalDateTime.now()).build())
                .isNonLocked(true).isCredentialsNonExpired(true).roles(Collections.singletonList(Role.builder().name("ROLE_USER").build())).build();
    }
}