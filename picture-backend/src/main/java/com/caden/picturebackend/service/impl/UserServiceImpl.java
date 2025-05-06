package com.caden.picturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caden.picturebackend.constant.UserConstant;
import com.caden.picturebackend.exception.BusinessException;
import com.caden.picturebackend.exception.ErrorCode;
import com.caden.picturebackend.exception.ThrowUtils;
import com.caden.picturebackend.model.dto.user.UserLoginRequest;
import com.caden.picturebackend.model.dto.user.UserQueryRequest;
import com.caden.picturebackend.model.entity.User;
import com.caden.picturebackend.model.enums.UserRoleEnum;
import com.caden.picturebackend.model.vo.LoginUserVO;
import com.caden.picturebackend.model.vo.UserVO;
import com.caden.picturebackend.service.UserService;
import com.caden.picturebackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenj
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-03-09 15:13:25
 */

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    /**
     * 用户注册
     *
     * @params userAccount 用户名
     * @params userPassword 密码
     * @params checkPassword 校验密码
     * return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1、校验参数
        if (StrUtil.hasBlank(userAccount, checkPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名长度过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        //2、检测用户名是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        //需要连同is_delete=1或0的都查出来

        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名已存在");
        }
        //3、密码加密
        String  encryptPassword = getEncryptPassword(userPassword);
        //4、用户信息写入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败,数据库错误");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
//        1、校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount,userPassword),ErrorCode.PARAMS_ERROR,"参数不能为空");
        ThrowUtils.throwIf(userAccount.length()<4,ErrorCode.PARAMS_ERROR,"用户名错误");
        ThrowUtils.throwIf(userPassword.length()<8,ErrorCode.PARAMS_ERROR,"密码错误");
//        2、将密码进行加密
        String  encryptPassword = getEncryptPassword(userPassword);
//        3、查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        if(user == null) {
            log.info("user login fail");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名或密码错误");
        }
//        4、保存用户登录状态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);

        UserLoginRequest  userLoginRequest = new UserLoginRequest();
        return this.getLoginUserVO(user);
    }

    /**
     * 脱敏后的用户信息
     * @param user
     * @return
     */

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO getLoginUser(HttpServletRequest request) {
        //判断是否已经登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null || currentUser.getId()==null ) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        currentUser = this.getById(currentUser.getId());
        if(currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return getLoginUserVO(currentUser);
    }

    @Override
    public boolean userLoginOut(HttpServletRequest request) {
//        1、判断是否已经登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtils.throwIf(userObj == null,ErrorCode.NOT_LOGIN_ERROR);
//        2、移除Sission中的用户
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取查询条件
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null,ErrorCode.PARAMS_ERROR,"请求参数为空");
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userAvatar = userQueryRequest.getUserAvatar();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(id),"id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userAccount),"user_account", userAccount);
        queryWrapper.like(ObjUtil.isNotEmpty(userName),"user_name", userName);
        queryWrapper.like(ObjUtil.isNotEmpty(userAvatar),"user_avatar", userAvatar);
        queryWrapper.like(ObjUtil.isNotEmpty(userProfile),"user_profile", userProfile);
        queryWrapper.like(ObjUtil.isNotEmpty(userRole),"user_role", userRole);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortOrder),sortOrder.equals("ascend"),sortField);
        return queryWrapper;
    }

    @Override
    public boolean isAdmin(LoginUserVO user) {
        if (user == null)
            return false;
        if (user.getUserRole().equals(UserConstant.ADMIN_ROLE))
            return true;
        return false;
    }

    public String getEncryptPassword(String password) {
        //加盐，混淆密码
        return DigestUtils.md5DigestAsHex((UserConstant.PASSWORD_SALT + password).getBytes());
    }
}




