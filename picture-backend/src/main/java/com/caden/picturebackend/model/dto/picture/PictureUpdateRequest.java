package com.caden.picturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String url;
    private String thumbnailUrl;
    private String name;
    private String introduction;
    private String category;
    private String tags;
    private Long picSize;
    private Integer picWidth;
    private Integer picHeight;
    private Double picScale;
    private String picFormat;
    private Long userId;
    /**
     * 空间ID
     */
    private Long spaceId;

}
