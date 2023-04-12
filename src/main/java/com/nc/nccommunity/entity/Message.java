package com.nc.nccommunity.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;


@Data
@ToString
public class Message {

    private int id;
    private int fromId;
    private int toId;
    private String conversationId;
    private String content;
    private int status;
    private Date createTime;
    
}
