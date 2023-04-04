package com.nc.nccommunity.util;

import com.nc.nccommunity.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息,用于代替session对象.
 */
@Slf4j
@Component
public class HostHolder {
	ThreadLocal<User> threadLocal = new ThreadLocal<>();
	
	public void setUser(User user) {
		threadLocal.set(user);
	}
	
	public User getUser() {
		return threadLocal.get();
	}
	
	public void clear(){
		threadLocal.remove();
	}
	
}
