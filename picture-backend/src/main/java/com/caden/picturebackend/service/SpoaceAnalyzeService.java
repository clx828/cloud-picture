package com.caden.picturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caden.picturebackend.model.dto.space.analyze.*;
import com.caden.picturebackend.model.entity.Space;
import com.caden.picturebackend.model.vo.LoginUserVO;
import com.caden.picturebackend.model.vo.analyze.*;

import java.util.List;

/**
 * @author Caden
 * @description 针对表【space】的数据库操作Service
 * @createDate 2023-05-08 16:08:09
 */

public interface SpoaceAnalyzeService extends IService<Space> {
    /**
     * 获取空间使用情况分析
     * @param spaceUsageAnalyzeRequest
     * @param loginUserVO
     * @return
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, LoginUserVO loginUserVO);

    /**
     * 获取空间分类情况分析
     * @param spaceCategoryAnalyzeRequest
     * @param loginUserVO
     * @return
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, LoginUserVO loginUserVO);
    /**
     * 获取空间标签情况分析
     * @param spaceTagAnalyzeRequest
     * @param loginUserVO
     * @return
     */
    List<SpaceTagAnalyzeAResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, LoginUserVO loginUserVO);

    /**
     * 获取空间图片大小情况分析
     * @param spaceSizeAnalyzeRequest
     * @param loginUserVO
     * @return
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, LoginUserVO loginUserVO);

    /**
     * 获取用户上传情况分析
     * @param spaceUserAnalyzeRequest
     * @param loginUserVO
     * @return
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, LoginUserVO loginUserVO);

    /**
     * 获取空间使用情况排行
     * @param spaceRankAnalyzeRequest
     * @param loginUserVO
     * @return
     */
    List<Space>  getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, LoginUserVO loginUserVO);
}
