package com.caden.picturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caden.picturebackend.exception.BusinessException;
import com.caden.picturebackend.exception.ErrorCode;
import com.caden.picturebackend.exception.ThrowUtils;
import com.caden.picturebackend.mapper.SpaceMapper;
import com.caden.picturebackend.model.dto.space.analyze.*;
import com.caden.picturebackend.model.entity.Picture;
import com.caden.picturebackend.model.entity.Space;
import com.caden.picturebackend.model.entity.User;
import com.caden.picturebackend.model.vo.LoginUserVO;
import com.caden.picturebackend.model.vo.SpaceVO;
import com.caden.picturebackend.model.vo.analyze.*;
import com.caden.picturebackend.service.PictureService;
import com.caden.picturebackend.service.SpaceService;
import com.caden.picturebackend.service.SpoaceAnalyzeService;
import com.caden.picturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.LazyInitializationExcludeFilter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    @Autowired
    private LazyInitializationExcludeFilter lazyInitializationExcludeFilter;

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
            spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
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
            spaceUsageAnalyzeResponse.setSizeUsageRatio(sizeusageRatio);
            spaceUsageAnalyzeResponse.setCountUsageRatio(countusageRatio);
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

    @Override
    public List<SpaceTagAnalyzeAResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, LoginUserVO loginUserVO) {
        //校验参数
        ThrowUtils.throwIf(spaceTagAnalyzeRequest==null,ErrorCode.PARAMS_ERROR,"请求参数错误");
        //全空间和公共空间
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest,loginUserVO);
        //统计图库的使用空间
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest,queryWrapper);
        queryWrapper.select("tags");
        List<String> tagsJsonStrList = pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil ::isNotEmpty)
                .map(Object::toString)
                .collect(Collectors.toList());

        //解析标签并统计
        Map<String,Long> tagCountMap = tagsJsonStrList.stream()
                .flatMap(tagsJsonStr-> JSONUtil.toList(tagsJsonStr,String.class).stream())
                .collect(Collectors.groupingBy(tag->tag,Collectors.counting()));

        //转化为响应对象，按照使用次数依次排序
        return tagCountMap.entrySet()
                .stream()
                .sorted((e1,e2) -> Long.compare(e2.getValue(),(e1.getValue())))
                .map(entry->new SpaceTagAnalyzeAResponse(entry.getKey(),entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, LoginUserVO loginUserVO) {
        //校验参数
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest==null,ErrorCode.PARAMS_ERROR,"请求参数错误");
        //全空间和公共空间
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest,loginUserVO);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest,queryWrapper);
        queryWrapper.select("pic_size");
        List<Long> sizeList =pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil ::isNotEmpty)
                .map(size->(Long)size)
                .collect(Collectors.toList());
        //定义图片区间范围
        Map<String,Long> sizeRangeMap = new LinkedHashMap<>();
        sizeRangeMap.put("<100KB", sizeList.stream().filter(size->size<100*1024).count());
        sizeRangeMap.put("100KB-500KB", sizeList.stream().filter(size->size>=100*1024&&size<500*1024).count());
        sizeRangeMap.put("500KB-1MB", sizeList.stream().filter(size->size>=500*1024&&size<1024*1024).count());
        sizeRangeMap.put("1MB-5MB", sizeList.stream().filter(size->size>=1024*1024&&size<5*1024*1024).count());
        //转化为响应对象
        return sizeRangeMap.entrySet().stream()
                .map(entry->new SpaceSizeAnalyzeResponse(entry.getKey(),entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, LoginUserVO loginUserVO) {
        //校验参数
        ThrowUtils.throwIf(spaceUserAnalyzeRequest==null,ErrorCode.PARAMS_ERROR,"请求参数错误");
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest,loginUserVO);
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest,queryWrapper);
        Long userId = spaceUserAnalyzeRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotNull(userId)&& userId>0,"user_id", userId);
        //补充查询条件每日、每周、每月
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension){
            case "day":
                queryWrapper.select("DATE_FORMAT(create_time,'%Y-%m-%d') as period", "count(*) as count");
                break;
            case "week":
                queryWrapper.select("YEARWEEK(create_time) as period", "count(*) as count");
                break;
            case "month":
                queryWrapper.select("DATE_FORMAT(create_time,'%Y-%m') as period", "count(*) as count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"时间维度参数错误");
        }
        //分组排序
        queryWrapper.groupBy("period").orderByAsc("period");

        //查询结果
        List<SpaceUserAnalyzeResponse> spaceUserAnalyzeResponseList =pictureService.getBaseMapper().selectMaps(queryWrapper)
                .stream()
                .map(result->{
                    String period =result.get("period").toString();
                    Long count = Long.parseLong(result.get("count").toString());
                    return new SpaceUserAnalyzeResponse(period,count);
                })
                .collect(Collectors.toList());
        return spaceUserAnalyzeResponseList;
    }


    @Override
    public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, LoginUserVO loginUser) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

        // 检查权限，仅管理员可以查看
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);

        // 构造查询条件
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "space_name", "user_id", "total_size")
                .orderByDesc("total_size")
                .last("limit " + spaceRankAnalyzeRequest.getTopN()); // 取前 N 名

        // 查询并封装结果
        return spaceService.list(queryWrapper);
    }
}
