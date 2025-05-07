package com.caden.picturebackend.model.vo.analyze;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceUsageAnalyzeResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    //已使用大小
    private Long usedSize;
    //总大小
    private Long maxSize;
    //使用率
    private Double sizeusageRatio;
    //当前图片数
    private Long usedCount;
    //最大图片数
    private Long maxCount;
    //使用率
    private Double countusageRatio;
}
