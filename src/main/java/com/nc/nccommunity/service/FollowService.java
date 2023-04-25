package com.nc.nccommunity.service;

import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.util.CommunityConstant;
import com.nc.nccommunity.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private UserService userService;
	
	//关注
	public void follow(int userId, int entityType, int entityId){
		String followeeKey = RedisUtil.getFolloweeRedisKey(userId, entityType);
		String followerKey = RedisUtil.getFollowerRedisKey(entityType, entityId);
		
		redisTemplate.execute(new SessionCallback() {
			@Override
			public Object execute(RedisOperations operations) throws DataAccessException {
				
				
				operations.multi();
				operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
				operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
				
				return operations.exec();
			}
		});
	}
	
	//取关
	public void unFollow(int userId, int entityType, int entityId){
		String followeeKey = RedisUtil.getFolloweeRedisKey(userId, entityType);
		String followerKey = RedisUtil.getFollowerRedisKey(entityType, entityId);
		
		redisTemplate.execute(new SessionCallback() {
			@Override
			public Object execute(RedisOperations operations) throws DataAccessException {
				
				
				operations.multi();
				operations.opsForZSet().remove(followerKey, userId);
				operations.opsForZSet().remove(followeeKey, entityId);
				
				return operations.exec();
			}
		});
	}
	
	//粉丝数
	public long getFollowerCount(int entityType, int entityId){
		String followerKey = RedisUtil.getFollowerRedisKey(entityType, entityId);
		return redisTemplate.opsForZSet().zCard(followerKey);
	}
	
	//关注数
	public long getFolloweeCount(int userId, int entityType){
		String followeeKey = RedisUtil.getFolloweeRedisKey(userId, entityType);
		return redisTemplate.opsForZSet().zCard(followeeKey);
	}
	
	//当前用户是否已关注该实体
	public boolean isFollower(int userId, int entityType, int entityId){
		String followeeKey = RedisUtil.getFolloweeRedisKey(userId, entityType);
		return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
	}
	
	//关注列表
	public List<Map<String, Object>> getFollowees(int userId, int offset, int limit){
		String followeeKey = RedisUtil.getFolloweeRedisKey(userId, ENTITY_TYPE_USER);
		Set<Integer> ids = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
		
		if(ids == null) return null;
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for(Integer id : ids){
			Map<String, Object> map = new HashMap<>();
			User user = userService.getUserById(id);
			map.put("user", user);
			Double score = redisTemplate.opsForZSet().score(followeeKey, id);
			map.put("followTime", new Date(score.longValue()));
			list.add(map);
		}
		
		return list;
	}
	
	//fans列表
	public List<Map<String, Object>> getFollowers(int userId, int offset, int limit){
		String followerKey = RedisUtil.getFollowerRedisKey(ENTITY_TYPE_USER, userId);
		Set<Integer> ids = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
		
		if(ids == null) return null;
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for(Integer id : ids){
			Map<String, Object> map = new HashMap<>();
			User user = userService.getUserById(id);
			map.put("user", user);
			Double score = redisTemplate.opsForZSet().score(followerKey, id);
			map.put("followTime", new Date(score.longValue()));
			list.add(map);
		}
		
		return list;
	}
	
}
