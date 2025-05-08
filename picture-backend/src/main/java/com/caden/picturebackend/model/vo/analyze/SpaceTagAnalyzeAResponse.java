package com.caden.picturebackend.model.vo.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceTagAnalyzeAResponse {
    private String tag;

    private Long count;
}
