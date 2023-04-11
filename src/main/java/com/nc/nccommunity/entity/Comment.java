package com.nc.nccommunity.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class Comment {

    private int id;
    private int userId;
    private int entityType;//被评论的实体的类型 1-帖子 2-评论
    private int entityId;//被评论的实体的Id
    private int targetId;//评论为回复时，被回复用户的Id
    private String content;
    private int status;//0-正常显示 1-不显示
    private Date createTime;

    
}
