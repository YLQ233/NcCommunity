package com.nc.nccommunity.service;

import com.nc.nccommunity.dao.LoginTicketMapper;
import com.nc.nccommunity.dao.UserMapper;
import com.nc.nccommunity.entity.LoginTicket;
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

import javax.servlet.http.HttpSession;
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
	@Autowired
	private LoginTicketMapper loginTicketMapper;
	@Value("${community.path.domain}")
	String domain;
	@Value("${server.servlet.context-path}")
	String contextPath;
	
	
	public User getUserById(int id){
		return userMapper.selectById(id);
	}
	public boolean isEmailExist(String email) {
		User user = userMapper.selectByEmail(email);
		return user != null;
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
		if(isEmailExist(user.getEmail())){
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
	
	public Map<String,Object> login(String username, String password, long expiredSeconds){
		Map<String,Object> map = new HashMap<String,Object>();
		//空值
		if (StringUtils.isBlank(username)) {
			map.put("usernameMsg", "账号不能为空!");
			return map;
		}
		if (StringUtils.isBlank(password)) {
			map.put("passwordMsg", "密码不能为空!");
			return map;
		}
		
		//验证
		User user = userMapper.selectByName(username);
		if(user == null) {
			map.put("usernameMsg", "该账号不存在!");
			return map;
		}
		password = CommunityUtil.md5(password + user.getSalt());
		if(!user.getPassword().equals(password)){
			map.put("passwordMsg", "密码不正确!");
			return map;
		}
		if(user.getStatus() == 0){
			map.put("usernameMsg", "该账号未激活!");
			return map;
		}
		
		//登录凭证
		String ticket = CommunityUtil.generateUUID();
		LoginTicket loginTicket = new LoginTicket();
		loginTicket.setUserId(user.getId());
		loginTicket.setTicket(ticket);
		loginTicket.setStatus(0);
		loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
		loginTicketMapper.insertLoginTicket(loginTicket);
		
		map.put("ticket", loginTicket.getTicket());
		
		return map;
	}
	
	public void logout(String ticket) {
		loginTicketMapper.updateStatus(ticket, 1);
	}
	
	public LoginTicket getLoginTicketByTicket(String ticket) {
		return loginTicketMapper.selectByTicket(ticket);
	}
	
	public int updateHeader(int id, String headerUrl){
		return userMapper.updateHeader(id, headerUrl);
	}
	
	public Map<String,Object> updatePassword(User user, String oldpw, String newpw){
		Map<String,Object> map = new HashMap<String,Object>();
		//null
		if (user == null) {
			throw new IllegalArgumentException("不可为空");
		}
		if (StringUtils.isBlank(oldpw)) {
			map.put("oldPasswordMsg", "原密码不能为空!");
			return map;
		}
		if (StringUtils.isBlank(newpw)) {
			map.put("newPasswordMsg", "新密码不能为空!");
			return map;
		}
		
		//old != pw
		oldpw = CommunityUtil.md5(oldpw + user.getSalt());
		if(!user.getPassword().equals(oldpw)){
			map.put("oldPasswordMsg", "原密码错误!");
			return map;
		}
		
		//save change
		userMapper.updatePassword(user.getId(), CommunityUtil.md5(newpw + user.getSalt()));
		
		return map;
	}
	
	public Map<String,Object> resetPassword (String email, String password){
		Map<String,Object> map = new HashMap<String,Object>();
		//null
		if(StringUtils.isBlank(password)){
			map.put("passwordMsg", "密码不能为空!");
			return map;
		}
		if(StringUtils.isBlank(email)){
			map.put("emailMsg", "邮箱不能为空!");
			return map;
		}
		
		//correct email
		if(userMapper.selectByEmail(email) == null){
			map.put("emailMsg", "邮箱未注册!");
		}
		
		//change
		User user = userMapper.selectByEmail(email);
		password = CommunityUtil.md5(password + user.getSalt());
		userMapper.updatePassword(user.getId(), password);
		map.put("user",user);
		
		return map;
	}
	
	public String getForgetCode(String email){
		Map<String,Object> map = new HashMap<String,Object>();
		//email
		String verifyCode = CommunityUtil.generateUUID().substring(0, 4);
		Context context = new Context();
		context.setVariable("email", email);
		context.setVariable("verifyCode", verifyCode);
		String content = templateEngine.process("/mail/forget", context);
		mailClient.sendMail(email, "找回密码", content);
		
		return verifyCode;
	}
	
}
