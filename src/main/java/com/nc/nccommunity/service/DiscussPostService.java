package com.nc.nccommunity.service;

import com.nc.nccommunity.dao.DiscussPostMapper;
import com.nc.nccommunity.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {
	@Autowired
	private DiscussPostMapper discussPostMapper;
	
	public List<DiscussPost> getDiscussPostList(int userId, int offset, int limit){
		return discussPostMapper.selectDiscussPostList(userId, offset, limit);
	}
	public int getDiscussPostRows(int userId){
		return discussPostMapper.selectDiscussPostRows(userId);
	}
}
