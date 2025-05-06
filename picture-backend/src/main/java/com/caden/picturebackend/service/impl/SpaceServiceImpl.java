package com.caden.picturebackend.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caden.picturebackend.exception.BusinessException;
import com.caden.picturebackend.exception.ErrorCode;
import com.caden.picturebackend.exception.ThrowUtils;
import com.caden.picturebackend.model.dto.space.SpaceQueryRequest;
import com.caden.picturebackend.model.entity.Space;
import com.caden.picturebackend.model.enums.SpaceLevelEnum;
import com.caden.picturebackend.model.enums.UserRoleEnum;
import com.caden.picturebackend.model.vo.LoginUserVO;
import com.caden.picturebackend.model.vo.SpaceVO;
import com.caden.picturebackend.model.vo.UserVO;
import com.caden.picturebackend.service.SpaceService;
import com.caden.picturebackend.mapper.SpaceMapper;
import com.caden.picturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
* @author chenj
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-03-29 12:47:38
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    private static final ConcurrentHashMap<Long, Object> locks = new ConcurrentHashMap<>();
    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        ThrowUtils.throwIf(space == null,ErrorCode.PARAMS_ERROR,"空间参数不能为空");
        SpaceVO spaceVO = new SpaceVO();
        BeanUtils.copyProperties(space, spaceVO);
        Long userId = space.getUserId();
        UserVO userVO = userService.getUserVO(userService.getById(userId));
        ThrowUtils.throwIf(userVO==null,ErrorCode.PARAMS_ERROR,"该用户不存在");
        spaceVO.setUserVO(userVO);
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize());
        List<SpaceVO> spaceVOList = new ArrayList<>();
        List<Space> spaceList =  spacePage.getRecords();
        for (Space space : spaceList) {
            spaceVOList.add(getSpaceVO(space, request));
        }
        spaceVOPage.setTotal(spacePage.getTotal());
        spaceVOPage.setPages(spacePage.getPages());
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        if (add) {
            ThrowUtils.throwIf(spaceName == null, ErrorCode.PARAMS_ERROR,"空间名称不能为空");
            ThrowUtils.throwIf(spaceLevelEnum == null, ErrorCode.PARAMS_ERROR,"空间级别不能为空");
        }
        ThrowUtils.throwIf(spaceName.length()>20, ErrorCode.PARAMS_ERROR,"空间名称过长");
        ThrowUtils.throwIf(spaceLevel!=null && spaceLevelEnum == null, ErrorCode.PARAMS_ERROR,"空间等级不存在");
        return;
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }

        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();


        queryWrapper.eq(ObjUtil.isNotEmpty(userId),"user_id", userId);
        queryWrapper.like(ObjUtil.isNotEmpty(spaceName),"space_name", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel),"space_level", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType),"space_type", spaceType);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField),sortOrder.equals("ascend"),sortField);
        return queryWrapper;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        ThrowUtils.throwIf(spaceLevelEnum == null,ErrorCode.PARAMS_ERROR,"未知用户等级用户");
        space.setMaxSize(spaceLevelEnum.getMaxSize());
        space.setMaxCount(spaceLevelEnum.getMaxCount());
    }

    @Override
    //@Transactional spring的事务注解
    public Long addSpace(Space space, LoginUserVO loginUserVO) {
        //1.检验参数
        this.validSpace(space, true);
        this.fillSpaceBySpaceLevel(space);
        //2.补全参数
        Long userId = loginUserVO.getId();
        space.setUserId(userId);
        space.setCreateTime(new Date());
        space.setEditTime(new Date());
        space.setUpdateTime(new Date());

        //2.校验权限
        if (space.getSpaceLevel() != SpaceLevelEnum.COMMON.getValue() && !Objects.equals(loginUserVO.getUserRole(), UserRoleEnum.ADMIN.getValue())) {
            String template = "无权限创建%s的空间";
            String message = String.format(
                    template,
                    SpaceLevelEnum.getEnumByValue(space.getSpaceLevel()).getName()
            );
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, message);
        }
        //3.一个用户只能创建一个私有空间，使用本地锁+事务控制，ps：这个str常量池不容易回收，可以使用hashMap,存储、使用完后移除
//        String lock = String.valueOf(space.getUserId()).intern();
        // 定义全局锁池
        Object lock = locks.computeIfAbsent(userId, k -> new Object());
        synchronized (lock) {
            try{
                //查询当前啊用户空间是否已经存在
                Long newSpaceId = transactionTemplate.execute(status -> {
                    boolean exists = this.lambdaQuery()
                            .eq(Space::getUserId, userId)
                            .exists();
                    ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "当前用户空间已存在");
                    boolean result = this.save(space);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "数据库操作异常");
                    return space.getId();
                });
                return newSpaceId;
            }catch (Exception e){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,e.getMessage());
            }finally {
                locks.remove(userId);
            }
        }
    }
}

