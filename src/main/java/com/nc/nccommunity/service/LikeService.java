package com.nc.nccommunity.service;

import com.nc.nccommunity.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
	@Qualifier("redisTemplate")
	@Autowired
	private RedisTemplate template;
	
	//like
	public void like(int userId, int entityType, int entityId, int authorId){
		String entityKey = RedisUtil.getEntityRedisKey(entityType,entityId);
		String userKey = RedisUtil.getUserRedisKey(authorId);
		Boolean isMember = template.opsForSet().isMember(entityKey,userId);
		template.execute(new SessionCallback() {
			@Override
			public Object execute(RedisOperations operations) throws DataAccessException {
				operations.multi();
				if(isMember){//取消赞
					template.opsForSet().remove(entityKey,userId);
					template.opsForValue().decrement(userKey);
				}else{
					template.opsForSet().add(entityKey,userId);
					template.opsForValue().increment(userKey);
				}
				return operations.exec();
			}
		});
	}
	
	//count
	public long countLikeEntity(int entityType, int entityId){
		String key = RedisUtil.getEntityRedisKey(entityType,entityId);
		BoundSetOperations op = template.boundSetOps(key);
		
		long cnt = op.size();
		return cnt;
	}
	
	
	//if liked
	public boolean ifLiked(int userId, int entityType, int entityId){
		String key = RedisUtil.getEntityRedisKey(entityType,entityId);
		BoundSetOperations op = template.boundSetOps(key);
		return op.isMember(userId);
	}
	
	//user收到的赞数量
	public Integer countLikeUser(int userId){
		String userKey = RedisUtil.getUserRedisKey(userId);
		Integer	i = (Integer) template.opsForValue().get(userKey);
		return i==null ? 0 : i.intValue();
	}
	
}
