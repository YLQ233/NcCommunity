package com.nc.nccommunity.service;

import com.nc.nccommunity.dao.CommentMapper;
import com.nc.nccommunity.dao.DiscussPostMapper;
import com.nc.nccommunity.entity.Comment;
import com.nc.nccommunity.util.CommunityConstant;
import com.nc.nccommunity.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {
	@Autowired
	private CommentMapper commentMapper;
	@Autowired
	private DiscussPostMapper discussPostMapper;
	@Autowired
	private SensitiveFilter sensitiveFilter;
	
	public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
		return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
	}
	
	public int findCountByEntity(int entityType, int entityId) {
		return commentMapper.selectCountByEntity(entityType, entityId);
	}
	
	@Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
	public int addComment(Comment comment) {
		if(comment == null){
			throw new IllegalArgumentException("参数不能为空!!!");
		}
		//添加评论
		comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
		comment.setContent(sensitiveFilter.filter(comment.getContent()));
		int rows = commentMapper.insertComment(comment);
		
		//评论dPost时，更新评论数
		if(comment.getEntityType() == ENTITY_TYPE_POST){
			int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
			discussPostMapper.updateCommentCount(comment.getEntityId(), count);
		}
		
		return rows;
	}
	
	public Comment findCommentById(int id) {
		return commentMapper.selectCommentById(id);
	}
	
	public List<Comment> findUserComments(int userId, int offset, int limit) {
		return commentMapper.selectCommentsByUserId(userId, offset, limit);
	}
	
	public int findUserCount(int userId) {
		return commentMapper.selectCountByUser(userId);
	}
	
	
}
