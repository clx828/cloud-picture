package com.caden.picturebackend.controller;


import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caden.picturebackend.annotation.AuthCheck;
import com.caden.picturebackend.common.BaseResponse;
import com.caden.picturebackend.common.DeleteRequest;
import com.caden.picturebackend.common.ResultUtils;
import com.caden.picturebackend.constant.UserConstant;
import com.caden.picturebackend.exception.ErrorCode;
import com.caden.picturebackend.exception.ThrowUtils;
import com.caden.picturebackend.manager.CacheManage;
import com.caden.picturebackend.model.dto.picture.*;
import com.caden.picturebackend.model.dto.space.*;
import com.caden.picturebackend.model.entity.Picture;
import com.caden.picturebackend.model.entity.Space;
import com.caden.picturebackend.model.entity.User;
import com.caden.picturebackend.model.enums.PictureReviewStatusEnum;
import com.caden.picturebackend.model.enums.SpaceLevelEnum;
import com.caden.picturebackend.model.vo.LoginUserVO;
import com.caden.picturebackend.model.vo.PictureTagCategory;
import com.caden.picturebackend.model.vo.PictureVO;
import com.caden.picturebackend.model.vo.SpaceVO;
import com.caden.picturebackend.service.PictureService;
import com.caden.picturebackend.service.SpaceService;
import com.caden.picturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/space")
@Slf4j
public class SpaceController {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        LoginUserVO loginUserVO = userService.getLoginUser(request);
        ThrowUtils.throwIf(spaceAddRequest==null,ErrorCode.NOT_LOGIN_ERROR);
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        Long spaceId = spaceService.addSpace(space,loginUserVO);
        return ResultUtils.success(spaceId);
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        LoginUserVO loginUserVO = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUserVO==null,ErrorCode.NOT_LOGIN_ERROR);
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        spaceService.validSpace(space,false);
        Space oldSpace = spaceService.getById(space.getId());
        ThrowUtils.throwIf(oldSpace == null,ErrorCode.PARAMS_ERROR,"空间不存在");
        BeanUtils.copyProperties(space, oldSpace);
        boolean result = spaceService.updateById(oldSpace);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"数据库操作失败");
        return ResultUtils.success(true );
    }
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
     public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        spaceService.validSpace(space,false);
        spaceService.fillSpaceBySpaceLevel(space);
        Space oldSpace = spaceService.getById(space.getId());
        BeanUtils.copyProperties(space, oldSpace);
        boolean result = spaceService.updateById(oldSpace);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"数据库操作异常");
        return ResultUtils.success(true );
    }
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest==null||deleteRequest.getId()<=0,ErrorCode.PARAMS_ERROR);
        boolean result = spaceService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"数据库操作异常");
        return ResultUtils.success(true );
    }

    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id<=0,ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceService.getSpaceVO(space,request));
    }

    @PostMapping("/get/vo/page")
    public BaseResponse<Page<SpaceVO>> getSpaceVOPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceQueryRequest==null,ErrorCode.PARAMS_ERROR);
        Page<Space> spacePage = spaceService.page(new Page<>(spaceQueryRequest.getCurrent(), spaceQueryRequest.getPageSize()),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spaceService.getSpaceVOPage(spacePage,request));
    }
    @GetMapping("/level/list")
    public BaseResponse<List<SpaceLevel>> getSpaceLevelList() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getName(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()))
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }

}
