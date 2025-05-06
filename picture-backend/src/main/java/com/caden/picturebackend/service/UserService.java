package com.caden.picturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.caden.picturebackend.model.dto.user.UserQueryRequest;
import com.caden.picturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caden.picturebackend.model.vo.LoginUserVO;
import com.caden.picturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author chenj
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2025-03-09 15:13:25
 */
public interface UserService extends IService<User> {
    long userRegister(String userAccount, String userPassword, String checkPassword);

    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    LoginUserVO getLoginUserVO(User user);

    LoginUserVO getLoginUser(HttpServletRequest request);

    /**
     * 加密密码
     * @param password
     * @return
     */
    String getEncryptPassword(String password);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLoginOut(HttpServletRequest request);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    boolean isAdmin(LoginUserVO user);

}
