package com.nc.nccommunity;

import com.nc.nccommunity.dao.DiscussPostMapper;
import com.nc.nccommunity.dao.LoginTicketMapper;
import com.nc.nccommunity.dao.UserMapper;
import com.nc.nccommunity.entity.DiscussPost;
import com.nc.nccommunity.entity.LoginTicket;
import com.nc.nccommunity.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
class MapperTests {
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private DiscussPostMapper discussPostMapper;
	
	@Autowired
	private LoginTicketMapper loginTicketMapper;
	
	@Test
	public void testSelectUser() {
		User user = userMapper.selectById(101);
		System.out.println(user);
		
		user = userMapper.selectByName("liubei");
		System.out.println(user);
		
		user = userMapper.selectByEmail("nowcoder101@sina.com");
		System.out.println(user);
	}
	
	@Test
	public void testInsertUser() {
		User user = new User();
		user.setUsername("test");
		user.setPassword("123456");
		user.setSalt("abc");
		user.setEmail("test@qq.com");
		user.setHeaderUrl("http://www.nowcoder.com/101.png");
		user.setCreateTime(new Date());
		
		int rows = userMapper.insertUser(user);
		System.out.println(rows);
		System.out.println(user.getId());
	}
	
	@Test
	public void updateUser() {
		int rows = userMapper.updateStatus(150, 1);
		System.out.println(rows);
		
		rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
		System.out.println(rows);
		
		rows = userMapper.updatePassword(150, "hello");
		System.out.println(rows);
	}
	
	@Test
	public void testSelectPosts() {
		List<DiscussPost> list = discussPostMapper.selectDiscussPostList(149, 0, 10);
		for (DiscussPost post : list) {
			System.out.println(post);
		}
		
		int rows = discussPostMapper.selectDiscussPostRows(149);
		System.out.println(rows);
	}
	
	@Test
	public void testInsertLoginTicket() {
		LoginTicket loginTicket = new LoginTicket();
		loginTicket.setUserId(101);
		loginTicket.setTicket("abc");
		loginTicket.setStatus(0);
		loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
		
		loginTicketMapper.insertLoginTicket(loginTicket);
	}
	
	@Test
	public void testSelectLoginTicket() {
		LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
		System.out.println(loginTicket);
	}
	
	@Test
	public void testUpdateLoginTicket() {
		loginTicketMapper.updateStatus("abc", 1);
	}
	
}
