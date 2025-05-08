package com.caden.picturebackend.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceRankAnalyzeRequest implements Serializable {
    private static final long serialVersionUID = 4172614336069205856L;
    private Integer TopN = 10;

}
