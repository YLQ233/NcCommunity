package com.nc.nccommunity.service;

import com.nc.nccommunity.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {
	@Autowired
	private RedisTemplate redisTemplate;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");;
	
	public void recordUV(String ip){
		String redisKey = RedisUtil.getUVKey(dateFormat.format(new Date()));
		redisTemplate.opsForHyperLogLog().add(redisKey, ip);
	}
	
	public long calcUV(Date begin, Date end){
		if(begin == null || end == null){
			throw new IllegalArgumentException("日期参数不能为空");
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(begin);
		List<String> list = new ArrayList<String>();
		
		while(!calendar.getTime().after(end)){
			String key = RedisUtil.getUVKey(dateFormat.format(calendar.getTime()));
			list.add(key);
			calendar.add(Calendar.DATE, 1);
		}
		
		String redisKey = RedisUtil.getUVKey(dateFormat.format(begin),dateFormat.format(end));
		redisTemplate.opsForHyperLogLog().union(redisKey, list);
		return redisTemplate.opsForHyperLogLog().size(redisKey);
	}
	
	public void recordDAU(int userId){
		String redisKey = RedisUtil.getDAUKey(dateFormat.format(new Date()));
		redisTemplate.opsForValue().setBit(redisKey, userId, true);
	}
	
	public long calcDAU(Date begin, Date end){
		if(begin == null || end == null){
			throw new IllegalArgumentException("日期参数不能为空");
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(begin);
		List<byte[]> list = new ArrayList<>();
		
		while(!calendar.getTime().after(end)){
			String key = RedisUtil.getDAUKey(dateFormat.format(calendar.getTime()));
			list.add(key.getBytes());
			calendar.add(Calendar.DATE, 1);
		}
		
		String redisKey = RedisUtil.getDAUKey(dateFormat.format(begin),dateFormat.format(end));
		Object obj = redisTemplate.execute(new RedisCallback() {
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				connection.bitOp(RedisStringCommands.BitOperation.OR,
						redisKey.getBytes(),
						list.toArray(new byte[0][0]));
				return connection.bitCount(redisKey.getBytes());
			}
		});
		return (long) obj;
	}

}
