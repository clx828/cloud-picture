package com.caden.picturebackend.common;

import lombok.Data;

@Data
public class PageRequest {
    // 当前页码
    private int current = 1;
    // 页面大小
    private int pageSize = 10;
    // 排序字段
    private String sortField;
    // 排序方式(默认升序)
    private String sortOrder = "desc";
}
