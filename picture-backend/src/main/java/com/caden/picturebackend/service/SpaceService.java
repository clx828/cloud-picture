package com.caden.picturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caden.picturebackend.model.dto.space.SpaceQueryRequest;
import com.caden.picturebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caden.picturebackend.model.vo.LoginUserVO;
import com.caden.picturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author chenj
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-03-29 12:47:38
*/
public interface SpaceService extends IService<Space> {

    /**
     * 获取图片包装类（单条)
     */
    SpaceVO getSpaceVO(Space space,HttpServletRequest request);


    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);


    void validSpace(Space space, boolean add);

    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    void fillSpaceBySpaceLevel(Space space);

    Long addSpace(Space space, LoginUserVO loginUserVO);

    void checkSpaceAuth(Space space, LoginUserVO loginUserVO);
}
