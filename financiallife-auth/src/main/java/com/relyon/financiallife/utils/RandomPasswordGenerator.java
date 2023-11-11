package com.relyon.financiallife.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class RandomPasswordGenerator {

    private final SecureRandom secureRandom;

    private static final String LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBER = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()_-+=<>?/{}~";
    private static final String ALL_CHARACTERS = LOWERCASE_LETTERS + UPPERCASE_LETTERS + NUMBER + SPECIAL_CHARACTERS;

    public String generateRandomPassword(int passwordLength) {
        StringBuilder password = new StringBuilder();

        password.append(LOWERCASE_LETTERS.charAt(secureRandom.nextInt(LOWERCASE_LETTERS.length())));
        password.append(UPPERCASE_LETTERS.charAt(secureRandom.nextInt(UPPERCASE_LETTERS.length())));
        password.append(NUMBER.charAt(secureRandom.nextInt(NUMBER.length())));
        password.append(SPECIAL_CHARACTERS.charAt(secureRandom.nextInt(SPECIAL_CHARACTERS.length())));

        secureRandom.ints((long) passwordLength - 4, 0, ALL_CHARACTERS.length())
                .mapToObj(ALL_CHARACTERS::charAt)
                .forEach(password::append);

        char[] passwordArray = password.toString().toCharArray();
        int length = passwordArray.length;

        for (int i = 0; i < length; i++) {
            int j = secureRandom.nextInt(length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        log.info("Random password was generated...");
        return new String(passwordArray);
    }
}