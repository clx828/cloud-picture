package com.caden.picturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.caden.picturebackend.exception.BusinessException;
import com.caden.picturebackend.exception.ErrorCode;
import com.caden.picturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String)inputSource;
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected void processFile(Object inputSource, File file) {
        String fileUrl = (String)inputSource;
        HttpUtil.downloadFile(fileUrl,file);
    }

    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String)inputSource;
        //1.校验非空
        ThrowUtils.throwIf(StrUtil.isBlankIfStr(fileUrl), ErrorCode.PARAMS_ERROR);
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
}
