package com.caden.picturebackend.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceAnalyzeRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long spaceId;
    private boolean queryPublic;
    private boolean queryAll;

}
