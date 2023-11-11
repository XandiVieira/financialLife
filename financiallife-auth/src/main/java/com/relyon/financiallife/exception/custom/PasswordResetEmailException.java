package com.relyon.financiallife.exception.custom;

public class PasswordResetEmailException extends RuntimeException {
    public PasswordResetEmailException(String message) {
        super(message);
    }
}