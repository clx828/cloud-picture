package com.caden.picturebackend.api.aliyun.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 查询扩图任务响应类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetOutPaintingTaskResponse {
        /**
         * 请求唯一标识
         */
        @JsonProperty("request_id")
        private String requestId;

        /**
         * 输出信息
         */
        private Output output;

        /**
         * 使用情况
         */
        private Usage usage;

        /**
         * 表示任务的输出信息
         */
        @Data
        public static class Output {

            /**
             * 任务 ID
             */
            @JsonProperty("task_id")
            private String taskId;

            /**
             * 任务状态
             * <ul>
             *     <li>PENDING：排队中</li>
             *     <li>RUNNING：处理中</li>
             *     <li>SUCCEEDED：执行成功</li>
             *     <li>FAILED：执行失败</li>
             * </ul>
             */
            @JsonProperty("task_status")
            private String taskStatus;

            /**
             * 提交时间
             * 格式：YYYY-MM-DD HH:mm:ss.SSS
             */
            @JsonProperty("submit_time")
            private String submitTime;

            /**
             * 调度时间
             * 格式：YYYY-MM-DD HH:mm:ss.SSS
             */
            @JsonProperty("scheduled_time")
            private String scheduledTime;

            /**
             * 结束时间
             * 格式：YYYY-MM-DD HH:mm:ss.SSS
             */
            @JsonProperty("end_time")
            private String endTime;

            /**
             * 任务结果列表
             */
            private List<Result> results;

            /**
             * 任务指标信息
             */
            @JsonProperty("task_metrics")
            private TaskMetrics taskMetrics;
        }

        /**
         * 表示单个任务结果
         */
        @Data
        public static class Result {
            /**
             * 生成图像的URL
             */
            private String url;
        }

        /**
         * 表示任务的统计信息
         */
        @Data
        public static class TaskMetrics {
            /**
             * 总任务数
             */
            private Integer TOTAL;

            /**
             * 成功任务数
             */
            private Integer SUCCEEDED;

            /**
             * 失败任务数
             */
            private Integer FAILED;
        }

        /**
         * 表示资源使用情况
         */
        @Data
        public static class Usage {
            /**
             * 生成的图片数量
             */
            @JsonProperty("image_count")
            private Integer imageCount;
        }
}
