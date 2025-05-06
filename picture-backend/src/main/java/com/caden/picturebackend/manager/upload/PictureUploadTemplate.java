package com.caden.picturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import com.caden.picturebackend.config.CosClientConfig;
import com.caden.picturebackend.exception.BusinessException;
import com.caden.picturebackend.exception.ErrorCode;
import com.caden.picturebackend.manager.CosManager;
import com.caden.picturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class PictureUploadTemplate {
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param inputSource
     * @param uploadPathPrefix
     * @return
     */

    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        //校验图片
        validPicture(inputSource);
        //图片上传地址
        String uuid = UUID.randomUUID().toString();
        String originalFileName = getOriginalFilename(inputSource);
        //自己拼接文件名
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFileName));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
            processFile(inputSource, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //获取到图片处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectsList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectsList)) {
                CIObject compressedCiObject = objectsList.get(0);
                CIObject thumbCiObject = objectsList.get(1);
                return buildResult(originalFileName, compressedCiObject, thumbCiObject);
            }
            UploadPictureResult uploadPictureResult = buildResult(imageInfo, uploadPath, originalFileName, file);
            return uploadPictureResult;
        } catch (IOException e) {
            log.error("file upload error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件失败");
        } finally {

        }
    }

    /**
     * 封装压缩后的返回结果
     *
     * @param originalFileName
     * @param compressedCiObject
     * @return
     */

    private UploadPictureResult buildResult(String originalFileName, CIObject compressedCiObject, CIObject thumbCiObject) {

        int width = compressedCiObject.getWidth();
        int height = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
        uploadPictureResult.setName(FileUtil.mainName(originalFileName));
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        uploadPictureResult.setPicWidth(width);
        uploadPictureResult.setPicHeight(height);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbCiObject.getKey());
        return uploadPictureResult;
    }


    /**
     * 获取文件名
     *
     * @param inputSource
     * @return
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 获取文件源
     *
     * @param inputSource
     */

    protected abstract void processFile(Object inputSource, File file) throws IOException;

    protected abstract void validPicture(Object inputSource);

    //清理临时文件
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error");
        }
    }

    /**
     * 构造请求参数
     *
     * @param imageInfo
     * @param uploadPath
     * @param originalFileName
     * @param file
     * @return
     */
    private UploadPictureResult buildResult(ImageInfo imageInfo, String uploadPath, String originalFileName, File file) {
        String format = imageInfo.getFormat();
        int width = imageInfo.getWidth();
        int height = imageInfo.getHeight();
        double picScale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        uploadPictureResult.setName(FileUtil.mainName(originalFileName));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(width);
        uploadPictureResult.setPicHeight(height);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(format);
        return uploadPictureResult;
    }

}
