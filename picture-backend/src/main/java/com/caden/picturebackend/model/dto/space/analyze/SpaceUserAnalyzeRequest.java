package com.caden.picturebackend.model.dto.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest{
    private Long userId;
    //时间维度
    private String timeDimension;
}
