package com.nc.nccommunity;

import com.nc.nccommunity.dao.*;
import com.nc.nccommunity.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = NcCommunityApplication.class)
class MapperTests {
	
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private DiscussPostMapper discussPostMapper;
	@Autowired
	private LoginTicketMapper loginTicketMapper;
	@Autowired
	private MessageMapper messageMapper;
	@Autowired
	private CommentMapper commentMapper;
	
	@Test
	public void testComment(){
		List<Comment> commentList = commentMapper.selectCommentsByUserId(153, 0, 100);
		Comment comment = commentMapper.selectCommentById(1);
		int count = commentMapper.selectCountByUser(153);
		
		for (Comment c : commentList) {
			System.out.println(c.getContent());
		}
		
		System.out.println("comment = " + comment.getContent());
		System.out.println("count = " + count);
	}
	
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
	
//	@Test
//	public void updateUser() {
//		int rows = userMapper.updateStatus(150, 1);
//		System.out.println(rows);
//
//		rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
//		System.out.println(rows);
//
//		rows = userMapper.updatePassword(150, "hello");
//		System.out.println(rows);
//	}
	
	@Test
	public void testSelectPosts() {
		List<DiscussPost> list = discussPostMapper.selectDiscussPostList(149, 0, 10, 0);
		for (DiscussPost post : list) {
			System.out.println(post);
		}
		
		int rows = discussPostMapper.selectDiscussPostRows(149);
		System.out.println(rows);
	}
	
	
	@Test
	public void testSelectLetters() {
		List<Message> list = messageMapper.selectConversations(111, 0, 20);
		for (Message message : list) {
//			System.out.println(message);
		}
		
		int count = messageMapper.selectConversationCount(111);
		System.out.println(count);

		list = messageMapper.selectLetters("111_112", 0, 10);
		for (Message message : list) {
			System.out.println(message);
		}

		count = messageMapper.selectLetterCount("111_112");
		System.out.println(count);

		count = messageMapper.selectUnreadLetterCount(131, "111_131");
		System.out.println(count);
	
	}
}
