package com.caden.picturebackend.aop;

import com.caden.picturebackend.annotation.AuthCheck;
import com.caden.picturebackend.exception.BusinessException;
import com.caden.picturebackend.exception.ErrorCode;
import com.caden.picturebackend.model.entity.User;
import com.caden.picturebackend.model.enums.UserRoleEnum;
import com.caden.picturebackend.model.vo.LoginUserVO;
import com.caden.picturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doIntercept(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取当前登录用户
        LoginUserVO loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleRnum = UserRoleEnum.getEnumByValue(mustRole);
        //如果不需要权限,放行
        if(mustRoleRnum == null){
            return joinPoint.proceed();
        }
        //以下代码：必须有权限才能通过
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if(userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if(UserRoleEnum.ADMIN.equals(mustRoleRnum)&&!UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }
}
