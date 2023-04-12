package com.nc.nccommunity.dao;

import com.nc.nccommunity.entity.Message;

import java.util.List;

public interface MessageMapper {
	
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
	
}
