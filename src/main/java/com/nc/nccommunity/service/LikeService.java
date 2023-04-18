package com.nc.nccommunity.service;

import com.nc.nccommunity.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
	@Qualifier("redisTemplate")
	@Autowired
	private RedisTemplate template;
	
	//like
	public void like(int userId, int entityType, int entityId){
		String key = RedisUtil.getRedisKey(entityType,entityId);
		BoundSetOperations op = template.boundSetOps(key);
		
		if(op.isMember(userId)){
			op.remove(userId);
		}else{
			op.add(userId);
		}
	}
	
	//count
	public long countLike(int entityType, int entityId){
		String key = RedisUtil.getRedisKey(entityType,entityId);
		BoundSetOperations op = template.boundSetOps(key);
		
		long cnt = op.size();
		return cnt;
	}
	
	
	//if liked
	public boolean ifLiked(int userId, int entityType, int entityId){
		String key = RedisUtil.getRedisKey(entityType,entityId);
		BoundSetOperations op = template.boundSetOps(key);
		return op.isMember(userId);
	}
	
}
