package com.caden.picturebackend.manager;

import cn.hutool.core.io.FileUtil;
import com.caden.picturebackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private COSClient cosClient;

    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(),key, file);
        return cosClient.putObject(putObjectRequest);
    }

    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(),key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传并解析图片
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(),key, file);
        //对图片进行处理
        PicOperations picOperations = new PicOperations();
        //表示返回原图信息
        picOperations.setIsPicInfo(1);
        //图片压缩（改成webp）
        List<PicOperations.Rule> rules = new ArrayList<>();
        String webpKey = FileUtil.mainName(key)+".webp";
        PicOperations.Rule combinedRule = new PicOperations.Rule();
        combinedRule.setFileId(webpKey);
        combinedRule.setBucket(cosClientConfig.getBucket());
        combinedRule.setRule("imageMogr2/format/webp");
        rules.add(combinedRule);
        //缩略图处理
        PicOperations.Rule thumbnailRule = new PicOperations.Rule();
        String thumbnailUrlKey = FileUtil.mainName(key)+"_thumbnail."+FileUtil.getSuffix(key);
        thumbnailRule.setFileId(thumbnailUrlKey);
        thumbnailRule.setBucket(cosClientConfig.getBucket());
        thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>",256,256));
        rules.add(thumbnailRule);
        //构造处理参数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    public  void deleteObjectByKey(String key) {
        cosClient.deleteObject(cosClientConfig.getBucket(),key);
    }
}
