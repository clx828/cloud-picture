package com.caden.picturebackend.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.json.JSONUtil;
import com.caden.picturebackend.annotation.AuthCheck;
import com.caden.picturebackend.annotation.CacheEvictCustom;
import com.caden.picturebackend.api.aliyun.AliYunAiApi;
import com.caden.picturebackend.api.aliyun.model.CreateOutPaintingTaskResponse;
import com.caden.picturebackend.api.aliyun.model.GetOutPaintingTaskResponse;
import com.caden.picturebackend.common.BaseResponse;
import com.caden.picturebackend.common.DeleteRequest;
import com.caden.picturebackend.common.ResultUtils;
import com.caden.picturebackend.constant.UserConstant;
import com.caden.picturebackend.exception.BusinessException;
import com.caden.picturebackend.exception.ErrorCode;
import com.caden.picturebackend.exception.ThrowUtils;
import com.caden.picturebackend.manager.CacheManage;
import com.caden.picturebackend.model.dto.picture.*;
import com.caden.picturebackend.model.entity.Picture;
import com.caden.picturebackend.model.entity.Space;
import com.caden.picturebackend.model.enums.PictureReviewStatusEnum;
import com.caden.picturebackend.model.vo.LoginUserVO;
import com.caden.picturebackend.model.vo.PictureTagCategory;
import com.caden.picturebackend.model.vo.PictureVO;
import com.caden.picturebackend.model.vo.UserVO;
import com.caden.picturebackend.service.PictureService;
import com.caden.picturebackend.service.SpaceService;
import com.caden.picturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private SpaceService spaceService;
    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;

    @Resource
    private CacheManage cacheManage;
    @Resource
    private AliYunAiApi aliYunAiApi;

//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestParam("file") MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest request) {
        LoginUserVO loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile,pictureUploadRequest,loginUser);
        return ResultUtils.success(pictureVO);
    }
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest request) {
        LoginUserVO loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl,pictureUploadRequest,loginUser);
        return ResultUtils.success(pictureVO);
    }
    @PostMapping("/delete")
    @CacheEvictCustom(keyPrefix = "picture:getPictureVOByPageWithCache:",keySuffix = "*")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest==null||deleteRequest.getId()<=0,ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUserVO = userService.getLoginUser(request);
        Long id = deleteRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        boolean result = pictureService.deletePicture(oldPicture,loginUserVO);
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest){
    ThrowUtils.throwIf(pictureUpdateRequest==null,ErrorCode.PARAMS_ERROR);
    Picture picture = new Picture();
    BeanUtils.copyProperties(pictureUpdateRequest,picture);
    picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
    pictureService.validPicture(picture);
    Long id = pictureUpdateRequest.getId();
    Picture oldPicture = pictureService.getById(id);
    ThrowUtils.throwIf(oldPicture==null,ErrorCode.NOT_FOUND_ERROR);
    //操作数据库
    boolean result = pictureService.updateById(picture);
    ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
    return ResultUtils.success(true);

    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(Long id){
        ThrowUtils.throwIf(id==null,ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture==null,ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(picture);
    }

    @PostMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(Long id){
        ThrowUtils.throwIf(id==null,ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture==null,ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(pictureService.getPictureVO(picture));
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> getPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest){
        ThrowUtils.throwIf(pictureQueryRequest==null,ErrorCode.PARAMS_ERROR);
        long current = pictureQueryRequest.getCurrent();
        long size =pictureQueryRequest.getPageSize();
        Page<Picture> picturePage = pictureService.page(new Page<>(current,size), pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> getPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,HttpServletRequest request){
        ThrowUtils.throwIf(pictureQueryRequest==null,ErrorCode.PARAMS_ERROR,"参数不能为空");
        long current = pictureQueryRequest.getCurrent();
        long size =pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(size>20,ErrorCode.PARAMS_ERROR,"超出最大分页大小");
        //1.先将请求数据转为hash
        String queryCondtion = JSONUtil.toJsonStr(pictureQueryRequest);
        String hasKey = DigestUtil.md5Hex(queryCondtion);
        String redisKey = "picture:getPictureVOByPageWithCache:" + hasKey;
        //2.先从缓存中获取
        if(cacheManage.exists(redisKey)){
            //3.换成命中直接获取，并将其序列化返回到前端
            String pictureVOStr = String.valueOf(cacheManage.getCache(redisKey));
            Page<PictureVO> cachePictureVOPage = JSONUtil.toBean(pictureVOStr,Page.class);
            return ResultUtils.success(cachePictureVOPage);
        }
        //4.如果缓存不命中,从数据库中获取，并设置缓存
        Long spaceId = pictureQueryRequest.getSpaceId();
        if(spaceId==null){
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        }else{
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            LoginUserVO loginUserVO = userService.getLoginUser(request);
            if(!Objects.equals(space.getUserId(), loginUserVO.getId())){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限查看当前空间图片");
            }
        }
        Page<Picture> picturePage = pictureService.page(new Page<>(current,size), pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage= pictureService.getPictureVOPage(picturePage,request);
        String pictureVOStr = JSONUtil.toJsonStr(pictureVOPage);
        cacheManage.setCache(redisKey,pictureVOStr,500,1000);
        return ResultUtils.success(pictureVOPage);
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request){
        ThrowUtils.throwIf(pictureEditRequest==null,ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUserVO = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUserVO==null,ErrorCode.NOT_LOGIN_ERROR);
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest,picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        picture.setEditTime(new Date());
        pictureService.validPicture(picture);
        Long id = pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture==null,ErrorCode.NOT_FOUND_ERROR);
        //校验权限
        pictureService.checkPictureAuth(loginUserVO,oldPicture);
        pictureService.fillReviewParams(picture,loginUserVO);
        //操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,HttpServletRequest request){
        ThrowUtils.throwIf(pictureUploadByBatchRequest==null,ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUserVO = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUserVO==null,ErrorCode.NOT_LOGIN_ERROR);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest,loginUserVO);
        return ResultUtils.success(uploadCount);
    }

/**
 * 图片审核
 */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request){
        ThrowUtils.throwIf(pictureReviewRequest==null,ErrorCode.PARAMS_ERROR);
        LoginUserVO loginUserVO = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUserVO==null,ErrorCode.NOT_FOUND_ERROR);
        pictureService.doPictureReview(pictureReviewRequest,loginUserVO);
        return ResultUtils.success(true);
    }

    //TODO后期优化项目时把标签和分类放入数据库中
    @GetMapping("tag_category")
    public BaseResponse<PictureTagCategory> getPictureTagCategory(){
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门","搞笑","生活","高清","艺术","校园","背景","简历","创意");
        List<String> categoryList = Arrays.asList("默认","模版","电商","表情包","素材","海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> getPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,HttpServletRequest request){
        ThrowUtils.throwIf(pictureQueryRequest==null,ErrorCode.PARAMS_ERROR,"参数不能为空");
        long current = pictureQueryRequest.getCurrent();
        long size =pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(size>20,ErrorCode.PARAMS_ERROR,"超出最大分页大小");
        //1.先将请求数据转为hash
        String queryCondtion = JSONUtil.toJsonStr(pictureQueryRequest);
        String hasKey = DigestUtil.md5Hex(queryCondtion);
        String redisKey = "picture:getPictureVOByPageWithCache:" + hasKey;
        //2.先从缓存中获取
        if(cacheManage.exists(redisKey)){
            //3.换成命中直接获取，并将其序列化返回到前端
            String pictureVOStr = String.valueOf(cacheManage.getCache(redisKey));
            Page<PictureVO> cachePictureVOPage = JSONUtil.toBean(pictureVOStr,Page.class);
            return ResultUtils.success(cachePictureVOPage);
        }
        //4.如果缓存不命中,从数据库中获取，并设置缓存
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        Page<Picture> picturePage = pictureService.page(new Page<>(current,size), pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage= pictureService.getPictureVOPage(picturePage,request);
        String pictureVOStr = JSONUtil.toJsonStr(pictureVOPage);
        cacheManage.setCache(redisKey,pictureVOStr,500,1000);
        return ResultUtils.success(pictureVOPage);
    }

//    @PostMapping("/search/similar")
//    public BaseResponse<List<PictureVO>> searchSimilarPictures(@RequestBody String imgUrl ,HttpServletRequest request){
//        ThrowUtils.throwIf(imgUrl==null,ErrorCode.PARAMS_ERROR,"参数不能为空");
//        LoginUserVO loginUserVO = userService.getLoginUser(request);
//        ThrowUtils.throwIf(loginUserVO==null,ErrorCode.NOT_LOGIN_ERROR);
//        List<PictureVO> pictureVOList = pictureService.searchSimilarPictures(imgUrl,loginUserVO);
//    }

    /**
     * 创建 AI 扩图任务
     */
    @PostMapping("/out_painting/create_task")
//    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(@RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
                                                                                    HttpServletRequest request) {
        if (createPictureOutPaintingTaskRequest == null || (createPictureOutPaintingTaskRequest.getPictureId() == null&& createPictureOutPaintingTaskRequest.getBaseImageUrl() == null)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVO loginUser = userService.getLoginUser(request);
        CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 查询 AI 扩图任务
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
        String status = task.getOutput().getTaskStatus();
        log.info("任务状态:{}",status);
        if (status.equals("SUCCEEDED")){
            task.getOutput().getResults().get(0).setUrl(pictureService.uploadPictureByUrl(task.getOutput().getResults().get(0).getUrl()));
        }
        return ResultUtils.success(task);
    }

}
