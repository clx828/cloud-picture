package com.caden.picturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceEditRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;


}
