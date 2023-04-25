package com.nc.nccommunity.service;


import com.nc.nccommunity.dao.MessageMapper;
import com.nc.nccommunity.entity.Message;
import com.nc.nccommunity.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Arrays;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    public int findUnreadLetterCount(int userId, String conversationId) {
        return messageMapper.selectUnreadLetterCount(userId, conversationId);
    }
    
    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }
    
    public int readMessage(List<Integer> ids){
        return messageMapper.updateMessageStatus(ids, 1);
    }
    
    public int deleteMessage(int id){
        return messageMapper.updateMessageStatus(Arrays.asList(new Integer[]{id}), 2);
    }
    
    public Message getLatestMsg(int userId, String topic){
        return messageMapper.selectLatestMsg(userId, topic);
    }
    
    public int getGroupCnt(int userId, String topic){
        return messageMapper.selectGroupCnt(userId, topic);
    }
    
    public int getUnreadMsgCnt(int userId, String topic){
        return messageMapper.selectUnreadMsgCnt(userId, topic);
    }
    
    public List<Message> getMsgList(int userId, String topic, int offset, int limit){
        return messageMapper.selectMsgList(userId, topic, offset, limit);
    }
    
}
