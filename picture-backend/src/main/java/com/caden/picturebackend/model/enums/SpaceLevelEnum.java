package com.caden.picturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum SpaceLevelEnum {
    COMMON("普通版",0,100,100L * 1024 * 1024),
    PROFESSIONAL("专业版",1,1000,1000L * 1024 *  1024),
    FLAGSHIP("旗舰版",2,10000,10000L * 1024 * 1024);
    private String name;
    private int value;
    private long maxCount;
    private long maxSize;

    SpaceLevelEnum(String name, int value, long maxCount, long maxSize) {
        this.name = name;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }
    /**
     * 根据value获取枚举
     */
    public static SpaceLevelEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceLevelEnum spaceLevelEnum : SpaceLevelEnum.values()) {
            if (spaceLevelEnum.value == value) {
                return spaceLevelEnum;
            }
        }
        return null;
    }
}
