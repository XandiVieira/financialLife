package com.relyon.financiallife.model.permissions.enums;

public enum ProtectedPermissionEnum {
    PERMISSION_CREATE("permission:create"),
    PERMISSION_UPDATE("permission:update"),
    PERMISSION_DELETE("permission:delete");

    private final String permissionName;

    ProtectedPermissionEnum(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getPermissionName() {
        return permissionName;
    }
}