package com.nc.nccommunity.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class User {

    private int id;
    private String username;
    private String password;
    private String salt;//保存的pw后面拼接的随机字符串
    private String email;
    private int type;//用户类型  0-普通
    private int status;//激活1 / 未激活0
    private String activationCode;//注册时发送到email的激活码
    private String headerUrl;
    private Date createTime;

}
