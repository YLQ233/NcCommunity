package com.nc.nccommunity.dao;

import com.nc.nccommunity.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
//私信
	//会话列表
	List<Message> selectConversations(int userId, int offset, int limit);
	
	//会话数量
	int selectConversationCount(int userId);
	
	// 查询某个会话所包含的私信列表.
	List<Message> selectLetters(String conversationId, int offset, int limit);
	
	// 查询某个会话所包含的私信数量.
	int selectLetterCount(String conversationId);
	
	// 查询未读私信的数量
	int selectUnreadLetterCount(int userId, String conversationId);
	
	// 添加
	int insertMessage(Message message);
	
	// 更新消息状态
	int updateMessageStatus(List ids, int status);
	
//系统通知
	//newest msg
	Message selectLatestMsg(int userId, String topic);
	
	//groupCount
	int selectGroupCnt(int userId, String topic);
	
	//UnreadMsgCnt
	int selectUnreadMsgCnt(int userId, String topic);
	
	//msgList
	List<Message> selectMsgList(int userId, String topic, int offset, int limit);
	
}
