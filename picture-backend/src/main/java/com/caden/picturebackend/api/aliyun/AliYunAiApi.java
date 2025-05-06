package com.caden.picturebackend.api.aliyun;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.caden.picturebackend.api.aliyun.model.CreateOutPaintingTaskRequest;
import com.caden.picturebackend.api.aliyun.model.CreateOutPaintingTaskResponse;
import com.caden.picturebackend.api.aliyun.model.GetOutPaintingTaskResponse;
import com.caden.picturebackend.exception.ErrorCode;
import com.caden.picturebackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AliYunAiApi {
    @Value("${aliyunAi.apikey}")
    private  String API_KEY;

    //创建任务地址
    private  String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/image-synthesis";

    //查询创建任务结果
    private  String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";
    /**
     * 创建图像处理任务
     * @param createOutPaintingTaskRequest
     * @return
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        //发送请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header("Authorization","Bearer "+API_KEY)
                .header("X-DashScope-Async","enable")
                .header("Content-Type","application/json")
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));
        //处理响应
        try( HttpResponse httpResponse = httpRequest.execute()) {
           if (!httpResponse.isOk()){
               log.error("创建图像处理任务失败,请求地址:{},请求参数:{},响应状态码:{},响应内容:{}",CREATE_OUT_PAINTING_TASK_URL,
                       JSONUtil.toJsonStr(createOutPaintingTaskRequest),httpResponse.getStatus(),httpResponse.body());
               ThrowUtils.throwIf(true, ErrorCode.OPERATION_ERROR,"AI图片处理失败");
           }
           CreateOutPaintingTaskResponse createOutPaintingTaskResponse = JSONUtil.toBean(httpResponse.body(), CreateOutPaintingTaskResponse.class);
           if (createOutPaintingTaskResponse.getCode()!=null){
               createOutPaintingTaskResponse.getMessage();
               log.error("请求异常:{}", createOutPaintingTaskResponse.getMessage());
               ThrowUtils.throwIf(true, ErrorCode.OPERATION_ERROR,"AI图片处理失败");
           }
           return createOutPaintingTaskResponse;
        }
    }
    //查询任务状态
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId),ErrorCode.PARAMS_ERROR,"任务id不能为空");
        try(HttpResponse httpResponse = HttpRequest.get(String.format(GET_OUT_PAINTING_TASK_URL,taskId))
                .header("Authorization","Bearer "+API_KEY).execute()){
            if (!httpResponse.isOk()){
                log.error("查询任务状态失败,请求地址:{},请求参数:{},响应状态码:{},响应内容:{}",GET_OUT_PAINTING_TASK_URL,
                        taskId,httpResponse.getStatus(),httpResponse.body());
                ThrowUtils.throwIf(true, ErrorCode.OPERATION_ERROR,"AI图片处理失败");
            }
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        }
    }
}
