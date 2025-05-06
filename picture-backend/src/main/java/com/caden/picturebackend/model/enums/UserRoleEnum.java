package com.caden.picturebackend.model.enums;

import lombok.Getter;

@Getter
public enum UserRoleEnum {
    USER ("user", "普通用户"),
    ADMIN ("admin", "管理员");
    private final String value;
    private final String text;

    UserRoleEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }
    /**
     * 根据value获取枚举
     * @param value
     * @return 枚举值
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (UserRoleEnum roleEnum : UserRoleEnum.values()) {
            if (roleEnum.getValue().equals(value)) {
                return roleEnum;
            }
        }
        return null;
    }
}
