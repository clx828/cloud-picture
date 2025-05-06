package com.caden.picturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest  implements Serializable {
    private long id;
    private String fileUrl;
    private String fileName;
    /**
     * 空间ID
     */
    private Long spaceId;
    private static final long seriaVersionUID = 1L;
}
