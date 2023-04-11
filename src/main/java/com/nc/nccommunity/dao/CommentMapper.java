package com.nc.nccommunity.dao;

import com.nc.nccommunity.entity.Comment;

import java.util.List;

public interface CommentMapper {
	
	List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);
	
	int selectCountByEntity(int entityType, int entityId);
	
	int insertComment(Comment comment);
	
}