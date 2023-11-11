package com.relyon.financiallife.model.role.enums;

public enum RoleEnum {

    ROLE_ADMIN("admin"),
    ROLE_MANAGER("manager");

    private final String roleName;

    RoleEnum(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}