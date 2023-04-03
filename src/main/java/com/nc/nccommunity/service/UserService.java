package com.nc.nccommunity.service;

import com.nc.nccommunity.dao.UserMapper;
import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.util.CommunityConstant;
import com.nc.nccommunity.util.CommunityUtil;
import com.nc.nccommunity.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private MailClient mailClient;
	@Autowired
	private TemplateEngine templateEngine;
	@Value("${community.path.domain}")
	String domain;
	@Value("${server.servlet.context-path}")
	String contextPath;
	
	
	public User getUserById(int id){
		return userMapper.selectById(id);
	}
	
	public Map<String,Object> register(User user){
		Map<String,Object> map = new HashMap<String,Object>();
		//空值处理
		if(user == null){
			throw new IllegalArgumentException("不可为空");
		}
		if(StringUtils.isBlank(user.getUsername())){
			map.put("usernameMsg", "账号不能为空!");
			return map;
		}
		if (StringUtils.isBlank(user.getPassword())) {
			map.put("passwordMsg", "密码不能为空!");
			return map;
		}
		if (StringUtils.isBlank(user.getEmail())) {
			map.put("emailMsg", "邮箱不能为空!");
			return map;
		}
		
		//验证账号邮箱未注册
		if(userMapper.selectByName(user.getUsername()) != null){
			map.put("usernameMsg", "该账号已存在!");
			return map;
		}
		if(userMapper.selectByEmail(user.getEmail()) != null){
			map.put("emailMsg", "该邮箱已被注册!");
			return map;
		}
		
		//注册
		user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
		//密码 + md5随机字符串
		user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
		user.setType(0);//普通用户
		user.setStatus(0);//状态：未激活
		user.setActivationCode(CommunityUtil.generateUUID());
		user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
		user.setCreateTime(new Date());
		userMapper.insertUser(user);
		
		//激活邮箱
		Context context = new Context();
		context.setVariable("email",user.getEmail());
		String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
		context.setVariable("url",url);
		String content = templateEngine.process("/mail/activation",context);
		mailClient.sendMail(user.getEmail(),"Please activate your account",content);
		
		return map;
	}
	
	public int activation(int userId, String code){
		User user = userMapper.selectById(userId);
		if (user.getStatus() == 1) {
			return ACTIVATION_REPEAT;
		} else if (user.getActivationCode().equals(code)) {
			userMapper.updateStatus(userId, 1);
			return ACTIVATION_SUCCESS;
		} else {
			return ACTIVATION_FAILURE;
		}
	}
	
}