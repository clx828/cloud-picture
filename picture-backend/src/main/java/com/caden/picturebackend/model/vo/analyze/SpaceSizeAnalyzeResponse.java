package com.caden.picturebackend.model.vo.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceSizeAnalyzeResponse implements Serializable {
    private static final long serialVersionUID = 2631003674193293355L;
    private String sizeRange;
    private Long count;
}
