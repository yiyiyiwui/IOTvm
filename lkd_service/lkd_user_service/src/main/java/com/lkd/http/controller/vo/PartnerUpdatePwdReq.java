package com.lkd.http.controller.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PartnerUpdatePwdReq implements Serializable {
    private String password;
    private String newPassword;
}
