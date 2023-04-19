package com.nc.nccommunity.util;

public class RedisUtil {
	
	private static final String PREFIX_ENTITY = "like:entity";
	private static final String PREFIX_USER = "like:user";
	private static final String SPILT = ":";
	
	
	//like:entity:entityType:entityId -> set(userId)
	public static String getEntityRedisKey(int entityType, int entityId){
		return PREFIX_ENTITY + SPILT + entityType + SPILT + entityId;
	}
	
	//like:user:userId -> int
	public static String getUserRedisKey(int userId){//被赞的人（作者）
		return PREFIX_USER + SPILT + userId;
	}
	
	
}
