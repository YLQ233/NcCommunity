package com.nc.nccommunity.service;

import com.nc.nccommunity.dao.UserMapper;
import com.nc.nccommunity.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
	@Autowired
	private UserMapper userMapper;
	public User getUserById(int id){
		return userMapper.selectById(id);
	}
	public User getUserByName(String username){
		return userMapper.selectByName(username);
	}
	public User getUserByEmail(String email){
		return userMapper.selectByName(email);
	}
	public int addUser(User user){
		return userMapper.insertUser(user);
	}
	public int updateStatus(int id, int status){
		return userMapper.updateStatus(id, status);
	}
	public int updateHeader(int id, String headerUrl){
		return userMapper.updateHeader(id, headerUrl);
	}
	public int updatePassword(int id, String password){
		return userMapper.updatePassword(id, password);
	}
}
