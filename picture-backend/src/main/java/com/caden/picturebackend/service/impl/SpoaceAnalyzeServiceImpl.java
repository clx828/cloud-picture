package com.caden.picturebackend.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caden.picturebackend.exception.ErrorCode;
import com.caden.picturebackend.exception.ThrowUtils;
import com.caden.picturebackend.mapper.SpaceMapper;
import com.caden.picturebackend.model.dto.space.analyze.SpaceAnalyzeRequest;
import com.caden.picturebackend.model.dto.space.analyze.SpaceCategoryAnalyzeRequest;
import com.caden.picturebackend.model.dto.space.analyze.SpaceUsageAnalyzeRequest;
import com.caden.picturebackend.model.entity.Picture;
import com.caden.picturebackend.model.entity.Space;
import com.caden.picturebackend.model.entity.User;
import com.caden.picturebackend.model.vo.LoginUserVO;
import com.caden.picturebackend.model.vo.analyze.SpaceCategoryAnalyzeResponse;
import com.caden.picturebackend.model.vo.analyze.SpaceUsageAnalyzeResponse;
import com.caden.picturebackend.service.PictureService;
import com.caden.picturebackend.service.SpaceService;
import com.caden.picturebackend.service.SpoaceAnalyzeService;
import com.caden.picturebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SpoaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpoaceAnalyzeService {
    @Resource
    private UserService  userService;
    @Resource
    private SpaceService  spaceService;
    @Resource
    private PictureService pictureService;

    /**
     *校验不同请求空间权限
     * @param spaceAnalyzeRequest
     * @param loginUserVO
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, LoginUserVO loginUserVO){
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        if (queryPublic||queryAll){
            ThrowUtils.throwIf(!userService.isAdmin(loginUserVO), ErrorCode.NO_AUTH_ERROR);
        }else {
            Space space = this.getById(spaceId);
            ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            spaceService.checkSpaceAuth(space,loginUserVO);
        }
    }

    private void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper){
        //全空间分析
        if(spaceAnalyzeRequest.isQueryAll()){
            return;
        }
        //公共空间分析
        if(spaceAnalyzeRequest.isQueryPublic()){
            queryWrapper.isNull("space_id");
            return;
        }
        //私有空间分析
        if (spaceAnalyzeRequest.getSpaceId()!=null){
            queryWrapper.eq("space_id",spaceAnalyzeRequest.getSpaceId());
            return;
        }
        ThrowUtils.throwIf(true,ErrorCode.PARAMS_ERROR,"未指定查询范围");

    }

    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, LoginUserVO loginUserVO) {
        //校验参数
        //全空间和公共空间
        if (spaceUsageAnalyzeRequest.isQueryAll()||spaceUsageAnalyzeRequest.isQueryPublic()){
            checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest,loginUserVO);
            //统计图库的使用空间
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            fillAnalyzeQueryWrapper(spaceUsageAnalyzeRequest,queryWrapper);
            queryWrapper.select("pic_size");
            List<Object> pictureObjList = pictureService.getBaseMapper().selectObjs(queryWrapper);
            long usedSize = pictureObjList.stream().mapToLong(obj->(Long) obj).sum();
            long usedCount = pictureObjList.size();
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            //全空间和公共空间没有最大空间限制
            spaceUsageAnalyzeResponse.setUsedSize(usedSize);
            spaceUsageAnalyzeResponse.setUsedCount(usedCount);
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setSizeusageRatio(null);
            return spaceUsageAnalyzeResponse;
        }else {
            Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId==null||spaceId<=0,ErrorCode.PARAMS_ERROR,"未指定查询空间");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(space.getTotalSize());
            spaceUsageAnalyzeResponse.setMaxSize(space.getMaxSize());
            spaceUsageAnalyzeResponse.setUsedCount(space.getTotalCount());
            spaceUsageAnalyzeResponse.setMaxCount(space.getMaxCount());
            //使用率
            double sizeusageRatio = NumberUtil.round(space.getTotalSize()*100.0/space.getMaxSize(),2).doubleValue();
            double countusageRatio = NumberUtil.round(space.getTotalCount()*100.0/space.getMaxCount(),2).doubleValue();
            spaceUsageAnalyzeResponse.setSizeusageRatio(sizeusageRatio);
            spaceUsageAnalyzeResponse.setCountusageRatio(countusageRatio);
            return spaceUsageAnalyzeResponse;
        }
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, LoginUserVO loginUserVO) {
        //校验参数
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest==null,ErrorCode.PARAMS_ERROR,"请求参数错误");
        //全空间和公共空间
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest,loginUserVO);
        //统计图库的使用空间
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest,queryWrapper);
        queryWrapper.select("category","sum(pic_size) as totalSize","count(*) as count")
                .groupBy("category");
        return pictureService.getBaseMapper().selectMaps(queryWrapper)
                .stream()
                .map(result->{
                    String category = result.get("category").toString();
                    Long totalSize = Long.parseLong(result.get("totalSize").toString());
                    Long count = Long.parseLong(result.get("count").toString());
                    return new SpaceCategoryAnalyzeResponse(category,count,totalSize);
                })
                .collect(Collectors.toList());

    }
}
