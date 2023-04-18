package com.nc.nccommunity.util;

public class RedisUtil {
	
	private static final String PREFIX = "like:entity";
	private static final String SPILT = ":";
	
	
	//like:entity:entityType:entityId -> set(userId)
	public static String getRedisKey(int entityType, int entityId){
		return PREFIX + SPILT + entityType + SPILT + entityId;
	}
	
}
