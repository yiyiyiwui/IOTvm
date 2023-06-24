package com.lkd.http.controller.vo;
import lombok.Data;
@Data
public class LoginReq{
    /**
     * 账号(后台用)
     */
    private String loginName;
    /**
     * 密码
     */
    private String password;
    /**
     * 手机号(运维运营平台使用)
     */
    private String mobile;
    /**
     * 合作商账号(手机号)
     */
    private String account;
    /**
     *  验证码
     */
    private String code;
    /**
     * 客户端请求验证码的token
     */
    private String clientToken;
    /**
     * 登录类型 0：后台；1：运营运维端；2：合作商后台
     */
    private Integer loginType;
}