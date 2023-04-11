package com.nc.nccommunity.service;

import com.nc.nccommunity.dao.DiscussPostMapper;
import com.nc.nccommunity.entity.DiscussPost;
import com.nc.nccommunity.util.CommunityUtil;
import com.nc.nccommunity.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
	@Autowired
	private DiscussPostMapper discussPostMapper;
	@Autowired
	private SensitiveFilter sensitiveFilter;
	
	public List<DiscussPost> getDiscussPostList(int userId, int offset, int limit){
		return discussPostMapper.selectDiscussPostList(userId, offset, limit);
	}
	public int getDiscussPostRows(int userId){
		return discussPostMapper.selectDiscussPostRows(userId);
	}
	
	public int addDiscussPost(DiscussPost discussPost){
		if(discussPost == null){
			throw new IllegalArgumentException("插入的帖子为空");
		}
		//转义HTML<>
		discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
		discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
		//过滤敏感词
		discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
		discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));
		
		return discussPostMapper.insertDiscussPost(discussPost);
	}
	
	public DiscussPost findDiscussPost(int id){
		return discussPostMapper.selectDiscussPostById(id);
	}
	
	
}
