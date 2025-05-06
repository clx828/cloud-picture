package com.caden.picturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caden.picturebackend.api.aliyun.model.CreateOutPaintingTaskResponse;
import com.caden.picturebackend.model.dto.picture.*;
import com.caden.picturebackend.model.dto.user.UserQueryRequest;
import com.caden.picturebackend.model.entity.Picture;
import com.caden.picturebackend.model.entity.User;
import com.caden.picturebackend.model.vo.LoginUserVO;
import com.caden.picturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author chenj
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-03-12 19:13:07
*/
public interface PictureService extends IService<Picture> {

    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest , LoginUserVO loginUser);


    boolean isAdmin(LoginUserVO loginUser);

    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片包装类（单条)
     */
    PictureVO getPictureVO(Picture picture);


    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    void validPicture(Picture picture);

    void doPictureReview(PictureReviewRequest pictureReviewRequest, LoginUserVO loginUser);

    void fillReviewParams(Picture picture,LoginUserVO loginUser);

    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, LoginUserVO loginUser);

    void clearPictureFile(Picture picture);

    void checkPictureAuth(LoginUserVO loginUser, Picture picture);

    boolean deletePicture(Picture oldPicture, LoginUserVO loginUserVO);

    //Ai操作图片
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, LoginUserVO loginUser);

    /**
     * 通过Url上传图片，并返回图片信息
     */
    String uploadPictureByUrl(String url);
}
