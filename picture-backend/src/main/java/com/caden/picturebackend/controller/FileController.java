package com.caden.picturebackend.controller;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import com.caden.picturebackend.annotation.AuthCheck;
import com.caden.picturebackend.common.BaseResponse;
import com.caden.picturebackend.common.ResultUtils;
import com.caden.picturebackend.config.CosClientConfig;
import com.caden.picturebackend.constant.UserConstant;
import com.caden.picturebackend.exception.BusinessException;
import com.caden.picturebackend.exception.ErrorCode;
import com.caden.picturebackend.exception.ThrowUtils;
import com.caden.picturebackend.manager.CosManager;
import com.caden.picturebackend.model.dto.file.UploadPictureResult;
import com.caden.picturebackend.service.UserService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.utils.DateUtils;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private CosManager cosManager;
//    @Resource
//    private COSClient cosClient;
//
//    @Resource
//    CosClientConfig cosClientConfig;

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        //文件目录
        String fileName = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s",fileName);
        File file = null;
        try{
            file = File.createTempFile(filePath,null);
            multipartFile.transferTo(file);
            cosManager.putObject(filePath,file);
            return ResultUtils.success(filePath);
        } catch (IOException e) {
            log.error("file upload error"+filePath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传文件失败");
        }finally {
            if(file == null){
                return null;
            }
            boolean deleteResult = file.delete();
            if(!deleteResult){
                log.error("file delete error");
            }
        }
    }

    /**
     * 测试文件下载
     * @param filePath
     * @param response
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download")
    public void testDownloadFile(String filePath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try{
            COSObject cosObject = cosManager.getObject(filePath);
            cosObjectInput = cosObject.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            response.setContentType("application/octet-stream;charset=utf-8");
            response.setHeader("Content-Disposition","attachment;filename="+filePath);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            cosObjectInput.close();
        }
    }

}
