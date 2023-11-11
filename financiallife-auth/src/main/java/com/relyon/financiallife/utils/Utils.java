package com.relyon.financiallife.utils;

import com.relyon.financiallife.model.role.Role;
import com.relyon.financiallife.model.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {

    private Utils() {
    }

    public static String fullName(String firstName, String lastName) {
        if (firstName == null || lastName == null) {
            throw new IllegalArgumentException("First name and last name must not be null.");
        }
        return firstName + " " + lastName;
    }

    public static String localDateToStringPatternddMMyyyy(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null.");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    public static String localDateToString(String pattern, LocalDate date) {
        if (pattern == null || date == null) {
            throw new IllegalArgumentException("Pattern and date must not be null.");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    public static String localDateTimeToString(String pattern, LocalDateTime date) {
        if (pattern == null || date == null) {
            throw new IllegalArgumentException("Pattern and date must not be null.");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    public static String formatCellphoneNumberToString(long phoneNumber) {
        String phoneNumberString = String.valueOf(phoneNumber);

        if (phoneNumberString.length() != 11) {
            throw new IllegalArgumentException("Invalid phone number length. Expected 11 digits.");
        }

        return String.format(
                "(%s) %s-%s",
                phoneNumberString.substring(0, 2),
                phoneNumberString.substring(2, 7),
                phoneNumberString.substring(7)
        );
    }

    public static Long formatCellphoneNumberToLong(String cellphoneNumber) {
        if (cellphoneNumber == null) {
            throw new IllegalArgumentException("Cellphone number must not be null.");
        }
        String numericString = cellphoneNumber.replaceAll("\\D", "");
        return Long.valueOf(numericString);
    }

    public static String formatCellphoneNumberToSimpleString(String cellphoneNumber) {
        if (cellphoneNumber == null) {
            throw new IllegalArgumentException("Cellphone number must not be null.");
        }
        return cellphoneNumber.replaceAll("\\D", "");
    }

    public static Long cpfToLong(String cpf) {
        if (cpf == null) {
            throw new IllegalArgumentException("CPF must not be null.");
        }
        String numericString = cpf.replaceAll("\\D", "");
        return Long.valueOf(numericString);
    }

    public static String cpfFormattedToSimpleString(String cpf) {
        if (cpf == null) {
            throw new IllegalArgumentException("CPF must not be null.");
        }
        return cpf.replaceAll("\\D", "");
    }

    public static String longToFormattedCpf(Long cpf) {
        if (cpf == null) {
            throw new IllegalArgumentException("CPF must not be null.");
        }
        String cpfString = String.format("%011d", cpf);
        return cpfString.substring(0, 3) + "." + cpfString.substring(3, 6)
                + "." + cpfString.substring(6, 9) + "-" + cpfString.substring(9);
    }

    public static boolean isManager(User existingUser) {
        if (existingUser == null) {
            throw new IllegalArgumentException("User must not be null.");
        }
        return existingUser.getRoles().stream().map(Role::getName).toList().contains("ROLE_MANAGER");
    }
}