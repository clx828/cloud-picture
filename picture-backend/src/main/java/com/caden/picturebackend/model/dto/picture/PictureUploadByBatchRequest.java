package com.caden.picturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadByBatchRequest implements Serializable {
    private Integer count;
    private String searchText;
    private String pictureName;
    /**
     * 空间ID
     */
    private Long spaceId;
    private static final long seriaVersionUID = 1L;
} 
