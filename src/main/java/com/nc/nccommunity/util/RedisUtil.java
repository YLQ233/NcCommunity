package com.nc.nccommunity.util;

public class RedisUtil {
	private static final String SPLIT = ":";
	private static final String PREFIX_LIKE_ENTITY = "like:entity";
	private static final String PREFIX_LIKE_USER = "like:user";
	private static final String PREFIX_FOLLOWEE = "followee";
	private static final String PREFIX_FOLLOWER = "follower";
	private static final String PREFIX_KAPTCHA = "kaptcha";
	private static final String PREFIX_TICKET = "ticket";
	private static final String PREFIX_USER = "user";
	
	//like:entity:entityType:entityId -> set(userId)
	public static String getLikeEntityRedisKey(int entityType, int entityId){
		return PREFIX_LIKE_ENTITY + SPLIT + entityType + SPLIT + entityId;
	}
	
	//like:user:userId -> int
	public static String getLikeUserRedisKey(int userId){//被赞的人（作者）
		return PREFIX_LIKE_USER + SPLIT + userId;
	}
	
	// 某个用户关注的某类型的实体集合
	// followee:userId:entityType -> zset(entityId,now)
	public static String getFolloweeRedisKey(int userId, int entityType){
		return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
	}
	
	// 某个实体拥有的粉丝
	// follower:entityType:entityId -> zset(userId,now)
	public static String getFollowerRedisKey(int entityType, int entityId){
		return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
	}
	
	// 登录验证码
	public static String getKaptchaKey(String owner) {
		return PREFIX_KAPTCHA + SPLIT + owner;
	}
	
	// 登录的凭证
	public static String getTicketKey(String ticket) {
		return PREFIX_TICKET + SPLIT + ticket;
	}
	
	// 用户
	public static String getUserKey(int userId) {
		return PREFIX_USER + SPLIT + userId;
	}
	
}
