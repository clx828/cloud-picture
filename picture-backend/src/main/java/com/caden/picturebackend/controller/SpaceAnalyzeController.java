package com.caden.picturebackend.controller;

import com.caden.picturebackend.common.BaseResponse;
import com.caden.picturebackend.common.ResultUtils;
import com.caden.picturebackend.exception.ErrorCode;
import com.caden.picturebackend.exception.ThrowUtils;
import com.caden.picturebackend.model.dto.space.analyze.*;
import com.caden.picturebackend.model.entity.Space;
import com.caden.picturebackend.model.vo.LoginUserVO;
import com.caden.picturebackend.model.vo.analyze.*;
import com.caden.picturebackend.service.SpoaceAnalyzeService;
import com.caden.picturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR, "参数错误");
        LoginUserVO loginUserVO = userService.getLoginUser(request);
        return ResultUtils.success(spoaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUserVO));
    }
    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagAnalyzeAResponse>> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR, "参数错误");
        LoginUserVO loginUserVO = userService.getLoginUser(request);
        return ResultUtils.success(spoaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUserVO));
    }
    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {
    ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR, "参数错误");
    LoginUserVO loginUserVO = userService.getLoginUser(request);
    return ResultUtils.success(spoaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUserVO));
    }
    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR, "参数错误");
        LoginUserVO loginUserVO = userService.getLoginUser(request);
        return ResultUtils.success(spoaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUserVO));
    }
    @PostMapping("/rank")
    public BaseResponse<List<Space>> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR, "参数错误");
        LoginUserVO loginUserVO = userService.getLoginUser(request);
        return ResultUtils.success(spoaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUserVO));
    }
}
