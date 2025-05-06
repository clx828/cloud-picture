package com.caden.picturebackend.model.dto.picture;

import com.caden.picturebackend.api.aliyun.model.CreateOutPaintingTaskRequest;
import lombok.Data;
import java.io.Serializable;

@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long pictureId;
    private String baseImageUrl;
    private String prompt;
    private String function;
    private CreateOutPaintingTaskRequest.Parameters  parameters;
}
