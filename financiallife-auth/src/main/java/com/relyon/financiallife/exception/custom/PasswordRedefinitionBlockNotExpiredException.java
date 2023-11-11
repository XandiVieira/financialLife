package com.relyon.financiallife.exception.custom;

public class PasswordRedefinitionBlockNotExpiredException extends RuntimeException {
    public PasswordRedefinitionBlockNotExpiredException(String message) {
        super(message);
    }
}