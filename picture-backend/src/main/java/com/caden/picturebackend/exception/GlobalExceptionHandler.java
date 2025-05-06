package com.caden.picturebackend.exception;

import com.caden.picturebackend.common.BaseResponse;
import com.caden.picturebackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.model.AsmRelationshipUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> handlerBusinessException(BusinessException e) {
        return ResultUtils.error(e.getCode(), e.getMessage());
    }
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> handlerBusinessException(RuntimeException e) {
        log.error(e.getMessage(), e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
