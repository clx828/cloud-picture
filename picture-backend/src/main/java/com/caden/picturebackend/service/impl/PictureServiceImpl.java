package com.caden.picturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caden.picturebackend.api.aliyun.AliYunAiApi;
import com.caden.picturebackend.api.aliyun.model.CreateOutPaintingTaskRequest;
import com.caden.picturebackend.api.aliyun.model.CreateOutPaintingTaskResponse;
import com.caden.picturebackend.exception.BusinessException;
import com.caden.picturebackend.exception.ErrorCode;
import com.caden.picturebackend.exception.ThrowUtils;
import com.caden.picturebackend.manager.CosManager;
import com.caden.picturebackend.manager.upload.FilePictureUpload;
import com.caden.picturebackend.manager.upload.PictureUploadTemplate;
import com.caden.picturebackend.manager.upload.UrlPictureUpload;
import com.caden.picturebackend.model.dto.file.UploadPictureResult;
import com.caden.picturebackend.model.dto.picture.*;
import com.caden.picturebackend.model.entity.Picture;
import com.caden.picturebackend.model.entity.Space;
import com.caden.picturebackend.model.entity.User;
import com.caden.picturebackend.model.enums.PictureReviewStatusEnum;
import com.caden.picturebackend.model.enums.UserRoleEnum;
import com.caden.picturebackend.model.vo.LoginUserVO;
import com.caden.picturebackend.model.vo.PictureVO;
import com.caden.picturebackend.model.vo.UserVO;
import com.caden.picturebackend.service.PictureService;
import com.caden.picturebackend.mapper.PictureMapper;
import com.caden.picturebackend.service.SpaceService;
import com.caden.picturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author chenj
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-03-12 19:13:07
*/
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService {

//    @Resource
//    private  FileManager fileManager;
    @Resource
    private UserService userService;
    @Resource
    private FilePictureUpload filePictureUpload;
    @Resource
    private UrlPictureUpload urlPictureUpload;
    @Autowired
    private CosManager cosManager;
    @Autowired
    private SpaceService spaceService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private AliYunAiApi aliYunAiApi;

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, LoginUserVO loginUser) {
        //校验参数
        ThrowUtils.throwIf(loginUser == null,ErrorCode.NOT_LOGIN_ERROR);
        Long pictureId = null;
        Long spaceId = null;
        String pictureName = null;
        Picture oldPicture = null;
        if (pictureUploadRequest!=null){
            pictureId = pictureUploadRequest.getId();
            spaceId = pictureUploadRequest.getSpaceId();
            pictureName = pictureUploadRequest.getFileName() ;
        }
        if (spaceId != null){
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            if(!Objects.equals(space.getUserId(), loginUser.getId())){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限上传当前空间图片");
            }
            ThrowUtils.throwIf(space.getMaxCount()<=space.getTotalCount(),ErrorCode.OPERATION_ERROR,"空间图片数量已达上限");
            ThrowUtils.throwIf(space.getMaxSize()<=space.getTotalSize(),ErrorCode.OPERATION_ERROR,"空间图片总大小已达上限");
        }
        //判断是新增还是修改
        if (pictureId != null && pictureId>0) {
            oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 改为使用统一的权限校验
//            // 仅本人或管理员可编辑图片
//            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//            }
            // 校验空间是否一致
            // 没传 spaceId，则复用原有图片的 spaceId（这样也兼容了公共图库）
            if (spaceId == null) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                // 传了 spaceId，必须和原图片的空间 id 一致
                if (ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间 id 不一致");
                }
            }
        }
        //根据用户获取目录
        String uploadPathPrefix;
        if (spaceId == null){
            uploadPathPrefix = String.format("public/%s",loginUser.getId());
        }else {
            uploadPathPrefix = String.format("space/%s",spaceId);
        }
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        Picture picture = getPicture(loginUser, uploadPictureResult, pictureId);
        picture.setSpaceId(spaceId);
        Long finalSpaceId = spaceId;
        if (pictureName!=null){
            picture.setName(pictureName);
        }
        this.fillReviewParams(picture,loginUser);
        if ( spaceId== null){
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"图片上传失败，数据库操作异常");
            return PictureVO.objToVo(picture);
        }
        transactionTemplate.execute(status -> {
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"图片上传失败，数据库操作异常");
           boolean updateResult = spaceService.lambdaUpdate()
                    .eq(Space::getId, finalSpaceId)
                    .setSql("total_count = total_count + "+1)
                    .setSql("total_Size = total_size + " + uploadPictureResult.getPicSize())
                    .update();
            ThrowUtils.throwIf(!updateResult,ErrorCode.OPERATION_ERROR,"图片上传失败，空间数据库操作异常");
            return picture;
        });

        return PictureVO.objToVo(picture);
    }



    private static Picture getPicture(LoginUserVO loginUser, UploadPictureResult uploadPictureResult, Long pictureId) {
        String url = uploadPictureResult.getUrl();
        String name = uploadPictureResult.getName();
        Long picSize = uploadPictureResult.getPicSize();
        Integer picWidth = uploadPictureResult.getPicWidth();
        Integer picHeight = uploadPictureResult.getPicHeight();
        Double picScale = uploadPictureResult.getPicScale();
        String picFormat = uploadPictureResult.getPicFormat();
        String thumbnailUrl = uploadPictureResult.getThumbnailUrl();
        //构建入库信息
        Picture picture = new Picture();
        picture.setCategory("默认");
        picture.setUrl(url);
        picture.setName(name);
        picture.setPicSize(picSize);
        picture.setPicWidth(picWidth);
        picture.setPicHeight(picHeight);
        picture.setPicScale(picScale);
        picture.setPicFormat(picFormat);
        picture.setUserId(loginUser.getId());
        picture.setThumbnailUrl(thumbnailUrl);
        //操作数据库
        if (pictureId !=null && pictureId !=0){
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        return picture;
    }

    @Override
    public boolean isAdmin(LoginUserVO loginUser) {
        return loginUser != null && UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole());
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest==null){
            return queryWrapper;
        }
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        Boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        if (StrUtil.isNotBlank(searchText)){
            queryWrapper.and(qw->qw.like("name",searchText))
                    .or()
            .like("introduction",searchText);
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id),"id",id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId),"user_id",userId);
        queryWrapper.isNull(nullSpaceId, "space_id");
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId),"space_id",spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(category),"category",category);
        queryWrapper.like(ObjUtil.isNotEmpty(name),"name",name);
        queryWrapper.like(ObjUtil.isNotEmpty(introduction),"introduction",introduction);
        queryWrapper.like(ObjUtil.isNotEmpty(picFormat),"pic_format",picFormat);
        queryWrapper.eq(ObjUtil.isNotEmpty(category),"category",category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize),"pic_size",picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth),"pic_width",picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight),"pic_height",picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale),"pic_scale",picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus),"review_status",reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId),"reviewer_id",reviewerId);
        queryWrapper.like(ObjUtil.isNotEmpty(reviewMessage),"review_message",reviewMessage);
        if (CollUtil.isNotEmpty(tags)){
            for (String tag : tags){
                queryWrapper.like("tags","\""+tag+"\"");
            }
        }
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField),sortOrder.equals("ascend"),sortField);
        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVO(Picture picture) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = picture.getUserId();
        if (userId!=null&&userId!=0){
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)){
            return pictureVOPage;
        }
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
        //关联查询用户信息
        Set<Long> userIdSet  = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long,List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        //填充信息、
        pictureVOList.forEach(pictureVO -> {
            long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)){
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    //校验图片
    @Override
    public void validPicture(Picture picture) {
    ThrowUtils.throwIf(picture==null,ErrorCode.PARAMS_ERROR);
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        ThrowUtils.throwIf(ObjUtil.isNull(id),ErrorCode.PARAMS_ERROR,"id不能为空");
        if (StrUtil.isNotBlank(url)){
            ThrowUtils.throwIf(url.length()>1024,ErrorCode.PARAMS_ERROR,"url过长");
        }
        if (StrUtil.isNotBlank(introduction)){
            ThrowUtils.throwIf(introduction.length()>800,ErrorCode.PARAMS_ERROR,"简介过长");
        }
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, LoginUserVO loginUser) {
        //1.校验参数
        ThrowUtils.throwIf(pictureReviewRequest==null,ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus  = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        String reviewMessage  = pictureReviewRequest.getReviewMessage();
        ThrowUtils.throwIf((reviewStatusEnum==null || id == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)),ErrorCode.PARAMS_ERROR);
        //2。判断图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture==null,ErrorCode.NOT_FOUND_ERROR);
        //3.校验审核状态是否重复
        ThrowUtils.throwIf(oldPicture.getReviewStatus().equals(reviewStatus),ErrorCode.PARAMS_ERROR,"请勿重复审核!");
        //4.操作数据库
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest,updatePicture);
        updatePicture.setReviewTime(new Date());
        updatePicture.setReviewerId(loginUser.getId());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"数据库操作失败");
    }

    public void fillReviewParams(Picture picture,LoginUserVO loginUser){
        if(userService.isAdmin(loginUser)){
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
            picture.setReviewMessage("管理员自动通过");
            return;
        }
        picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
    }

    /**
     *抓取图片
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, LoginUserVO loginUser) {
        //校验参数
        Integer count = pictureUploadByBatchRequest.getCount();
        String searchText = pictureUploadByBatchRequest.getSearchText();
        String pictureName = pictureUploadByBatchRequest.getPictureName();

        ThrowUtils.throwIf(searchText==null,ErrorCode.PARAMS_ERROR,"采集内容不能为空");
        ThrowUtils.throwIf(count>30,ErrorCode.PARAMS_ERROR,"最多抓取30条");
        //抓取内容
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1",searchText);
        Document document ;
        try{
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败",e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"获取页面失败");
        }
        //解析内容
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isEmpty(div)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"获取元素失败");
        }
        Elements imgElementList  = div.select("img.mimg");
        //遍历元素
        int uploadCount = 0;
        for (Element imgElement : imgElementList){
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)){
                log.info("当前链接为空，自动跳过");
                continue;
            }
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1){
                fileUrl = fileUrl.substring(0,questionMarkIndex);
            }
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(fileUrl);
            pictureUploadRequest.setFileName(pictureName+(uploadCount+1));
            try{
                PictureVO pictureVO = this.uploadPicture(fileUrl,pictureUploadRequest,loginUser);
                log.info("图片上传成功，id为{}",pictureVO.getId());
                uploadCount++;
            }catch (Exception e){
                log.error("图片上传失败");
                continue;
            }
            if (uploadCount>= count){
                break;
            }
        }
        //上传图片
    return uploadCount;
    }

    @Async
    @Override
    public void clearPictureFile(Picture picture) {
        //判断该图片是否被多条数据使用（在改项目中使用不到，这里是一图一记录)
        String pictureUrl = picture.getUrl();
        long count = this.lambdaQuery()
                .eq(Picture::getUrl,pictureUrl)
                .count();
        if (count > 1){
        return;
        }
        cosManager.deleteObjectByKey(pictureUrl);
        //删除缩略图
        String thumbnailUrl = picture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)){
            cosManager.deleteObjectByKey(thumbnailUrl);
        }
    }

    @Override
    public void checkPictureAuth(LoginUserVO loginUser, Picture picture) {
        if (picture.getSpaceId() == null){
            ThrowUtils.throwIf(!userService.isAdmin(loginUser)&& !Objects.equals(picture.getUserId(), loginUser.getId()),ErrorCode.NO_AUTH_ERROR,"无权限编辑该图片");
        }else {
ThrowUtils.throwIf( !Objects.equals(picture.getUserId(), loginUser.getId()),ErrorCode.NO_AUTH_ERROR,"无权限编辑该图片");
        }
    }

    @Override
    public boolean deletePicture(Picture oldPicture, LoginUserVO loginUserVO) {
        ThrowUtils.throwIf(oldPicture==null,ErrorCode.NOT_FOUND_ERROR);
        //校验权限
        this.checkPictureAuth(loginUserVO,oldPicture);
        Long spaceId = oldPicture.getSpaceId();
        if (spaceId != null){
            transactionTemplate.execute(status -> {
                boolean result = this.removeById(oldPicture.getId());
                ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
                this.clearPictureFile(oldPicture);
                boolean updateResult = spaceService.lambdaUpdate()
                        .eq(Space::getId, oldPicture.getSpaceId())
                        .setSql("total_count = total_count - "+1)
                        .setSql("total_Size = total_size - " + oldPicture.getPicSize())
                        .update();
                ThrowUtils.throwIf(!updateResult,ErrorCode.OPERATION_ERROR,"图片删除失败，空间数据库操作异常");
                return result;
            });
            return true;
        }
        boolean  result = this.removeById(oldPicture.getId());
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"图片删除失败" );
        return true;
    }

    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, LoginUserVO loginUser) {
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
        if (createPictureOutPaintingTaskRequest.getPictureId()!= null&& createPictureOutPaintingTaskRequest.getPictureId()>0){
            String pictureUrl = this.getById(createPictureOutPaintingTaskRequest.getPictureId()).getUrl();
            input.setBaseImageUrl(pictureUrl);
        }else {
            input.setBaseImageUrl(createPictureOutPaintingTaskRequest.getBaseImageUrl());
        }

        input.setPrompt(createPictureOutPaintingTaskRequest.getPrompt());
        input.setFunction(createPictureOutPaintingTaskRequest.getFunction());
        CreateOutPaintingTaskRequest.Parameters parameters = createPictureOutPaintingTaskRequest.getParameters();
        CreateOutPaintingTaskRequest createOutPaintingTaskRequest = new CreateOutPaintingTaskRequest();
        createOutPaintingTaskRequest.setInput(input);
        createOutPaintingTaskRequest.setParameters(parameters);
        return aliYunAiApi.createOutPaintingTask(createOutPaintingTaskRequest);
    }

    @Override
    public String uploadPictureByUrl(String url) {
        String uploadPathPrefix = "public/ai";
        PictureUploadTemplate pictureUploadTemplate = urlPictureUpload;
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(url, uploadPathPrefix);
        return uploadPictureResult.getUrl();
    }

}




