package com.caden.picturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = -4034797432782663871L;
    //用户名
    private String userAccount;
    // 密码
    private String userPassword;
    // 校验两次密码是否相同
    private String checkPassword;
}
