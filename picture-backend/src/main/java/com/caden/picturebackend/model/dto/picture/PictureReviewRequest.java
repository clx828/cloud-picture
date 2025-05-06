package com.caden.picturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureReviewRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Integer reviewStatus;
    private String reviewMessage;
}
