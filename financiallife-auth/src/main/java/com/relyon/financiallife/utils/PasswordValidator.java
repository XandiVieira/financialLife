package com.relyon.financiallife.utils;

import com.relyon.financiallife.exception.custom.PasswordValidationException;
import com.relyon.financiallife.model.user.User;
import lombok.extern.slf4j.Slf4j;

import static com.relyon.financiallife.utils.Utils.*;

@Slf4j
public class PasswordValidator {

    private PasswordValidator() {
    }

    public static void validate(String password, String passwordConfirmation, User user) {
        log.info("Validating password...");
        checkPasswordsAreEqual(password, passwordConfirmation);
        checkMinimumLength(password);
        checkLettersAndNumbers(password);
        checkSequenceOfCharacters(password);
        checkUppercaseLetter(password);
        checkLowercaseLetter(password);
        checkSpecialCharacter(password);
        checkNoSequence(password);
        checkNoUserDataVariation(password, localDateToString("ddMMyyyy", user.getDateOfBirth()), "dateOfBirth");
        checkNoUserDataVariation(password, formatCellphoneNumberToSimpleString(user.getCellphoneNumber()), "cellphoneNumber");
        checkNoUserDataVariation(password, user.getEmail(), "email");
        checkNoUserDataVariation(password, cpfFormattedToSimpleString(user.getCpf()), "cpf");
        checkNoUserDataVariation(password, user.getFirstName(), "firstName");
        checkNoUserDataVariation(password, user.getLastName(), "lastName");
        log.info("Password validated...");
    }

    private static void checkPasswordsAreEqual(String password, String passwordConfirmation) {
        if (!password.equals(passwordConfirmation)) {
            throw new PasswordValidationException("Password and password confirmation do not match");
        }
    }

    private static void checkMinimumLength(String password) {
        if (password.length() < 8) {
            throw new PasswordValidationException("Password must have at least 8 characters");
        }
    }

    private static void checkLettersAndNumbers(String password) {
        if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*")) {
            throw new PasswordValidationException("Password must include both letters and numbers");
        }
    }

    private static void checkSequenceOfCharacters(String password) {
        if (password.matches(".*(\\w)\\1{3,}.*")) {
            throw new PasswordValidationException("Password must not contain a sequence of more than 3 repeated characters");
        }
    }

    private static void checkUppercaseLetter(String password) {
        if (!password.matches(".*[A-Z].*")) {
            throw new PasswordValidationException("Password must have at least one uppercase letter");
        }
    }

    private static void checkLowercaseLetter(String password) {
        if (!password.matches(".*[a-z].*")) {
            throw new PasswordValidationException("Password must have at least one lowercase letter");
        }
    }

    private static void checkSpecialCharacter(String password) {
        if (!password.matches(".*[!@#$%^&*()].*")) {
            throw new PasswordValidationException("Password must have at least one special character");
        }
    }

    private static void checkNoSequence(String password) {
        if (password.matches(".*(0123|1234|2345|3456|4567|5678|6789|9876|8765|7654|6543|5432|4321|3210).*")) {
            throw new PasswordValidationException("Password must not have numbers in ascending or descending sequence greater than 3 characters");
        }
    }

    public static void checkNoUserDataVariation(String password, String userData, String fieldName) {
        int substringLength = 4;

        if (userData != null) {
            for (int i = 0; i <= userData.length() - substringLength; i++) {
                String substring = userData.substring(i, i + substringLength);

                if (password.toLowerCase().contains(substring.toLowerCase())) {
                    throw new PasswordValidationException("Password must not contain variations of user (" + fieldName + ")");
                }
            }
        }
    }
}