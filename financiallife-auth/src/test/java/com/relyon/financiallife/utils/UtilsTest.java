package com.relyon.financiallife.utils;

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
class UtilsTest {

    @Test
    void fullName_ShouldReturnConcatenatedFullName() {
        String firstName = "John";
        String lastName = "Doe";
        String expectedFullName = "John Doe";

        String fullName = Utils.fullName(firstName, lastName);

        assertEquals(expectedFullName, fullName);
    }

    @Test
    void localDateToStringPattern_ddMMyyyy_ShouldReturnFormattedDate() {
        LocalDate date = LocalDate.of(2022, 5, 20);
        String expectedFormattedDate = "20/05/2022";

        String formattedDate = Utils.localDateToStringPatternddMMyyyy(date);

        assertEquals(expectedFormattedDate, formattedDate);
    }

    @Test
    void localDateToString_ShouldReturnFormattedDate() {
        String pattern = "dd/MM/yyyy";
        LocalDate date = LocalDate.of(2022, 5, 20);
        String expectedFormattedDate = "20/05/2022";

        String formattedDate = Utils.localDateToString(pattern, date);

        assertEquals(expectedFormattedDate, formattedDate);
    }

    @Test
    void localDateTimeToString_ShouldReturnFormattedDateTime() {
        String pattern = "dd/MM/yyyy HH:mm:ss";
        LocalDateTime dateTime = LocalDateTime.of(2022, 5, 20, 10, 30, 45);
        String expectedFormattedDateTime = "20/05/2022 10:30:45";

        String formattedDateTime = Utils.localDateTimeToString(pattern, dateTime);

        assertEquals(expectedFormattedDateTime, formattedDateTime);
    }

    @Test
    void formatCellphoneNumberToString_WithValidPhoneNumber_ShouldReturnFormattedPhoneNumber() {
        long phoneNumber = 11223344556L;
        String expectedFormattedPhoneNumber = "(11) 22334-4556";

        String formattedPhoneNumber = Utils.formatCellphoneNumberToString(phoneNumber);

        assertEquals(expectedFormattedPhoneNumber, formattedPhoneNumber);
    }

    @Test
    void formatCellphoneNumberToString_WithInvalidPhoneNumberLength_ShouldThrowIllegalArgumentException() {
        long phoneNumber = 11223344L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Utils.formatCellphoneNumberToString(phoneNumber));

        assertEquals("Invalid phone number length. Expected 11 digits.", exception.getMessage());
    }

    @Test
    void formatCellphoneNumberToLong_ShouldReturnNumericPhoneNumber() {
        String phoneNumberString = "(11) 22334-4556";
        long expectedPhoneNumber = 11223344556L;

        long phoneNumber = Utils.formatCellphoneNumberToLong(phoneNumberString);

        assertEquals(expectedPhoneNumber, phoneNumber);
    }

    @Test
    void formatCellphoneNumberToSimpleString_ShouldReturnNumericPhoneNumber() {
        String phoneNumberString = "(11) 22334-4556";
        String expectedSimplePhoneNumber = "11223344556";

        String simplePhoneNumber = Utils.formatCellphoneNumberToSimpleString(phoneNumberString);

        assertEquals(expectedSimplePhoneNumber, simplePhoneNumber);
    }

    @Test
    void cpfToLong_WithValidCpf_ShouldReturnNumericCpf() {
        String cpf = "123.456.789-00";
        long expectedNumericCpf = 12345678900L;

        long numericCpf = Utils.cpfToLong(cpf);

        assertEquals(expectedNumericCpf, numericCpf);
    }

    @Test
    void cpfFormattedToSimpleString_ShouldReturnNumericCpf() {
        String formattedCpf = "123.456.789-00";
        String expectedNumericCpf = "12345678900";

        String numericCpf = Utils.cpfFormattedToSimpleString(formattedCpf);

        assertEquals(expectedNumericCpf, numericCpf);
    }

    @Test
    void longToFormattedCpf_ShouldReturnFormattedCpf() {
        long cpf = 12345678900L;
        String expectedFormattedCpf = "123.456.789-00";

        String formattedCpf = Utils.longToFormattedCpf(cpf);

        assertEquals(expectedFormattedCpf, formattedCpf);
    }

    @Test
    void isManager_WithManagerUser_ShouldReturnTrue() {
        User user = createUser();

        boolean isManager = Utils.isManager(user);

        assertTrue(isManager);
    }

    @Test
    void isManager_WithNonManagerUser_ShouldReturnFalse() {
        User user = createUser();
        user.setRoles(Collections.emptyList());

        boolean isManager = Utils.isManager(user);

        assertFalse(isManager);
    }

    @Test
    void formatCellphoneNumberToLong_WithNullPhoneNumber_ShouldThrowIllegalArgumentException() {
        String phoneNumber = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Utils.formatCellphoneNumberToLong(phoneNumber));

        assertEquals("Cellphone number must not be null.", exception.getMessage());
    }

    @Test
    void formatCellphoneNumberToSimpleString_WithNullPhoneNumber_ShouldThrowIllegalArgumentException() {
        String phoneNumber = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Utils.formatCellphoneNumberToSimpleString(phoneNumber));

        assertEquals("Cellphone number must not be null.", exception.getMessage());
    }

    @Test
    void cpfToLong_WithNullCpf_ShouldThrowIllegalArgumentException() {
        String cpf = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Utils.cpfToLong(cpf));

        assertEquals("CPF must not be null.", exception.getMessage());
    }

    @Test
    void cpfFormattedToSimpleString_WithNullCpf_ShouldThrowIllegalArgumentException() {
        String cpf = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Utils.cpfFormattedToSimpleString(cpf));

        assertEquals("CPF must not be null.", exception.getMessage());
    }

    @Test
    void longToFormattedCpf_WithNullCpf_ShouldThrowIllegalArgumentException() {
        Long cpf = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Utils.longToFormattedCpf(cpf));

        assertEquals("CPF must not be null.", exception.getMessage());
    }

    @Test
    void isManager_WithNullUser_ShouldThrowIllegalArgumentException() {
        User user = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Utils.isManager(user));

        assertEquals("User must not be null.", exception.getMessage());
    }

    @Test
    void fullName_WithNullFirstName_ShouldThrowIllegalArgumentException() {
        String firstName = null;
        String lastName = "Doe";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Utils.fullName(firstName, lastName));

        assertEquals("First name and last name must not be null.", exception.getMessage());
    }

    @Test
    void fullName_WithNullLastName_ShouldThrowIllegalArgumentException() {
        String firstName = "John";
        String lastName = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Utils.fullName(firstName, lastName));

        assertEquals("First name and last name must not be null.", exception.getMessage());
    }

    @Test
    void localDateToStringPatternddMMyyyy_WithNullDate_ShouldThrowIllegalArgumentException() {
        LocalDate date = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Utils.localDateToStringPatternddMMyyyy(date));

        assertEquals("Date must not be null.", exception.getMessage());
    }

    @Test
    void localDateToString_WithNullPattern_ShouldThrowIllegalArgumentException() {
        String pattern = null;
        LocalDate date = LocalDate.of(2022, 5, 20);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Utils.localDateToString(pattern, date));

        assertEquals("Pattern and date must not be null.", exception.getMessage());
    }

    @Test
    void localDateToString_WithNullDate_ShouldThrowIllegalArgumentException() {
        String pattern = "dd/MM/yyyy";
        LocalDate date = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Utils.localDateToString(pattern, date));

        assertEquals("Pattern and date must not be null.", exception.getMessage());
    }

    @Test
    void localDateTimeToString_WithNullPattern_ShouldThrowIllegalArgumentException() {
        String pattern = null;
        LocalDateTime dateTime = LocalDateTime.of(2022, 5, 20, 10, 30, 45);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Utils.localDateTimeToString(pattern, dateTime));

        assertEquals("Pattern and date must not be null.", exception.getMessage());
    }

    @Test
    void localDateTimeToString_WithNullDate_ShouldThrowIllegalArgumentException() {
        String pattern = "dd/MM/yyyy HH:mm:ss";
        LocalDateTime dateTime = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Utils.localDateTimeToString(pattern, dateTime));

        assertEquals("Pattern and date must not be null.", exception.getMessage());
    }

    private User createUser() {
        return User.builder().id(1L).firstName("John").lastName("Doess").username("johndoe").dateOfBirth(LocalDate.of(1990, 1, 1))
                .cpf("015.357.159-11").cellphoneNumber("(51) 98740-2118").email("johndoe@example.com").password("password123").enabled(true).isNonExpired(true)
                .userExtras(UserExtras.builder().loginAttempts(0).lastLogin(LocalDateTime.now()).passwordRedefinitionAttempts(0).passwordRedefinitionBlockExpirationTime(LocalDateTime.now()).build())
                .isNonLocked(true).isCredentialsNonExpired(true).roles(Collections.singletonList(Role.builder().name("ROLE_MANAGER").build())).build();
    }
}