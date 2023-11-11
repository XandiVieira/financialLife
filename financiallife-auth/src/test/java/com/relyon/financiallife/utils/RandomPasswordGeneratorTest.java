package com.relyon.financiallife.utils;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomPasswordGeneratorTest {

    @Test
    void generateRandomPassword_ShouldReturnRandomPassword() {
        SecureRandom secureRandom = new SecureRandom();

        RandomPasswordGenerator passwordGenerator = new RandomPasswordGenerator(secureRandom);

        int passwordLength = 16;

        for (int i = 0; i < 50; i++) {
            String generatedPassword = passwordGenerator.generateRandomPassword(passwordLength);

            assertEquals(passwordLength, generatedPassword.length());
            assertTrue(generatedPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=<>?/{}~-]).+$"));
        }
    }
}