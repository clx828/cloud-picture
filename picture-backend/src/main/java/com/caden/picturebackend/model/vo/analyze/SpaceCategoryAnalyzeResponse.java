package com.caden.picturebackend.model.vo.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceCategoryAnalyzeResponse implements Serializable {
    public static final long serialVersionUID = 1L;
    private String category;
    private  Long count;
    private Long totalSize;
}
