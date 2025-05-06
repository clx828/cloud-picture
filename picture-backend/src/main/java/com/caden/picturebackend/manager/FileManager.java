package com.caden.picturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.caden.picturebackend.config.CosClientConfig;
import com.caden.picturebackend.exception.BusinessException;
import com.caden.picturebackend.exception.ErrorCode;
import com.caden.picturebackend.exception.ThrowUtils;
import com.caden.picturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Deprecated //已经弃用改为使用upload包下的具体实现类
public class FileManager {
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     * @param multipartFile
     * @param uploadPathPrefix
     * @return
     */

    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        //校验图片
        validPicture(multipartFile);
        String uuid = UUID.randomUUID().toString();
        String originalFileName = multipartFile.getOriginalFilename();
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFileName));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        File file = null;
        try{
            file = File.createTempFile(uploadPath,null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath,file);
            // 获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            String format = imageInfo.getFormat();
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double picScale = NumberUtil.round(width *1.0 / height,2).doubleValue();

            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() +"/"+ uploadPath);
            uploadPictureResult.setName(FileUtil.mainName(originalFileName));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(width);
            uploadPictureResult.setPicHeight(height);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(format);
            return uploadPictureResult;
        } catch (IOException e) {
            log.error("file upload error",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传文件失败");
        }finally {

        }
    }


    public UploadPictureResult uploadPictureByUrl(String fileUrl, String uploadPathPrefix) {
        //校验图片
        validPicture(fileUrl);
        String uuid = UUID.randomUUID().toString();
        String originalFileName = FileUtil.mainName(fileUrl);
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFileName));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        File file = null;
        try{
            file = File.createTempFile(uploadPath,null);
            HttpUtil.downloadFile(fileUrl,file);
//            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath,file);
            // 获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            String format = imageInfo.getFormat();
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double picScale = NumberUtil.round(width *1.0 / height,2).doubleValue();

            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() +"/"+ uploadPath);
            uploadPictureResult.setName(FileUtil.mainName(originalFileName));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(width);
            uploadPictureResult.setPicHeight(height);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(format);
            return uploadPictureResult;
        } catch (IOException e) {
            log.error("file upload error",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传文件失败");
        }finally {
            this.deleteTempFile(file);
        }
    }

    private void validPicture(String fileUrl) {
        //1.校验非空
        ThrowUtils.throwIf(StrUtil.isBlankIfStr(fileUrl),ErrorCode.PARAMS_ERROR);
        //2.校验Url格式
        try{
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"URL格式不正确");
        }
        //3.校验Url协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://")&&!fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR,"仅支持 HTTP 或 HTTPS 协议文件地址");
        //4.发送HEAD请求校验文件是否存在
        HttpResponse httpResponse = null;
        try{
            httpResponse = HttpUtil.createRequest(Method.HEAD,fileUrl)
                    .execute();
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            String contentType = httpResponse.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png", "image/jpg", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),ErrorCode.PARAMS_ERROR,"文件类型错误");
            }
            //6.文件大小校验
            String contentLengthStr = httpResponse.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
               try{
                   long contentLength = Long.parseLong(contentLengthStr);
                   final long ONE_M = 1024*1024;
                   ThrowUtils.throwIf(contentLength > 2*ONE_M,ErrorCode.PARAMS_ERROR,"文件大小不能超过2MB");
               }catch (NumberFormatException e) {
                   throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件大小格式错误");
               }
            }
        }finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
    }

    //校验图片
    private void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null , ErrorCode.PARAMS_ERROR,"文件不能为空");
        long fileSize = multipartFile.getSize();
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(fileSize>2*ONE_MB,ErrorCode.PARAMS_ERROR,"文件大小不能超过2MB");
        //检验文件后缀
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "jpeg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(suffix),ErrorCode.PARAMS_ERROR,"文件类型错误");
    }

    //清理临时文件
    public void deleteTempFile(File file) {
        if(file == null){
            return;
        }
        boolean deleteResult = file.delete();
        if(!deleteResult){
            log.error("file delete error");
        }
    }
}
