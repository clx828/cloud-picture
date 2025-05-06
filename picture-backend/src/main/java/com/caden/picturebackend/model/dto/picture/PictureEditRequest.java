package com.caden.picturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureEditRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String url;
    private String thumbnailUrl;
    private String name;
    private String introduction;
    private String category;
    private List<String> tags;
    private Long picSize;
    private Integer picWidth;
    private Integer picHeight;
    private Double picScale;
    private String picFormat;
    /**
     * 空间ID
     */
    private Long spaceId;
}
