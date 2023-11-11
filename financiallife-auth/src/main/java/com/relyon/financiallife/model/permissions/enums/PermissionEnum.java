package com.relyon.financiallife.model.permissions.enums;

public enum PermissionEnum {
    USER_VIEW("user:view"),
    USER_CREATE("user:create"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete"),
    ROLE_VIEW("role:view"),
    ROLE_CREATE("role:create"),
    ROLE_UPDATE("role:update"),
    ROLE_DELETE("role:delete"),
    PERMISSION_VIEW("permission:view"),
    PERMISSION_CREATE("permission:create"),
    PERMISSION_UPDATE("permission:update"),
    PERMISSION_DELETE("permission:delete");

    private final String permissionName;

    PermissionEnum(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getPermissionName() {
        return permissionName;
    }
}