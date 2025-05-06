package com.caden.picturebackend.model.enums;

import io.swagger.models.auth.In;
import lombok.Getter;

@Getter
public enum PictureReviewStatusEnum {
    REVIEWING (0, "待审核"),
    PASS(1,"通过"),
    REJECT (2, "拒绝");
    private final Integer value;
    private final String text;

    PictureReviewStatusEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }
    /**
     * 根据value获取枚举
     * @param value
     * @return 枚举值
     */
    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (PictureReviewStatusEnum pictureReviewStatusEnum : PictureReviewStatusEnum.values()) {
            if (pictureReviewStatusEnum.value == value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }
}
