package com.caden.picturebackend.model.dto.file;

import lombok.Data;

import java.io.Serializable;

@Data
public class UploadPictureResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 图片URL
     */
    private String url;
    private String thumbnailUrl;

    /**
     * 图片名称
     */
    private String name;


    /**
     * 图片大小
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

}
