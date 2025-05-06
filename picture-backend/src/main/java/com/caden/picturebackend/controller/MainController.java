package com.caden.picturebackend.controller;

import com.caden.picturebackend.common.BaseResponse;
import com.caden.picturebackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MainController {
    @GetMapping("/health")
    public BaseResponse<?> health() {
        return ResultUtils.success("ok!");
    }
}
