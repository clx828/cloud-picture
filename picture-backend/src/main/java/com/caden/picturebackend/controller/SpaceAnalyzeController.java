package com.caden.picturebackend.controller;

import com.caden.picturebackend.common.BaseResponse;
import com.caden.picturebackend.common.ResultUtils;
import com.caden.picturebackend.exception.ErrorCode;
import com.caden.picturebackend.exception.ThrowUtils;
import com.caden.picturebackend.model.dto.space.analyze.SpaceCategoryAnalyzeRequest;
import com.caden.picturebackend.model.dto.space.analyze.SpaceUsageAnalyzeRequest;
import com.caden.picturebackend.model.vo.LoginUserVO;
import com.caden.picturebackend.model.vo.analyze.SpaceCategoryAnalyzeResponse;
import com.caden.picturebackend.model.vo.analyze.SpaceUsageAnalyzeResponse;
import com.caden.picturebackend.service.SpoaceAnalyzeService;
import com.caden.picturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {
    @Resource
    private SpoaceAnalyzeService spoaceAnalyzeService;
    @Resource
    private UserService userService;

    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR, "参数错误");
        LoginUserVO loginUserVO = userService.getLoginUser(request);
        return ResultUtils.success(spoaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUserVO));
    }
    @PostMapping("/category")
    public Object getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR, "参数错误");
        LoginUserVO loginUserVO = userService.getLoginUser(request);
        return ResultUtils.success(spoaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUserVO));
    }
}
