package com.relyon.financiallife.exception.custom;

public class PasswordResetInvalidTokenException extends RuntimeException {
    public PasswordResetInvalidTokenException(String message) {
        super(message);
    }
}