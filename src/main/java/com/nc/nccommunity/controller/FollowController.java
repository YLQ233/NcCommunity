package com.nc.nccommunity.controller;

import com.nc.nccommunity.annotation.LoginRequired;
import com.nc.nccommunity.entity.Event;
import com.nc.nccommunity.entity.Page;
import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.event.EventProducer;
import com.nc.nccommunity.service.FollowService;
import com.nc.nccommunity.service.UserService;
import com.nc.nccommunity.util.CommunityConstant;
import com.nc.nccommunity.util.CommunityUtil;
import com.nc.nccommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
	@Autowired
	private FollowService followService;
	@Autowired
	private HostHolder hostHolder;
	@Autowired
	private UserService userService;
	@Autowired
	private EventProducer eventProducer;
	
//	@LoginRequired
	@PostMapping("/follow")
	@ResponseBody
	public String follow(int entityType, int entityId) {
		User user = hostHolder.getUser();
		followService.follow(user.getId(), entityType, entityId);
		
		//系统通知
		Event event = new Event().setEntityType(entityType)
				.setEntityId(entityId)
				.setUserId(user.getId())
				.setEntityUserId(entityId)
				.setTopic(TOPIC_FOLLOW);
		
		eventProducer.fireEvent(event);
		
		return CommunityUtil.getJSONString(0, "已关注!");
	}
	
//	@LoginRequired
	@PostMapping("/unfollow")
	@ResponseBody
	public String unFollow(int entityType, int entityId) {
		User user = hostHolder.getUser();
		followService.unFollow(user.getId(), entityType, entityId);
		return CommunityUtil.getJSONString(0, "已取消关注!");
	}
	
	
	private boolean judgeFollowed(int userId) {
		User user = hostHolder.getUser();
		if(user == null)	return false;
		return followService.isFollower(user.getId(), ENTITY_TYPE_USER, userId);
	}
	
	@GetMapping("followees/{userId}")
	public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
		User user = userService.getUserById(userId);
		if (user == null) {
			throw new RuntimeException("该用户不存在!");
		}
		model.addAttribute("user", user);
		
		page.setLimit(5);
		page.setPath("/followees/" + userId);
		page.setRows((int) followService.getFolloweeCount(userId, ENTITY_TYPE_USER));
		
		List<Map<String, Object>> list = followService.getFollowees(userId, page.getOffset(), page.getLimit());
		if (list != null) {
			for (Map<String, Object> map : list) {
				User u = (User) map.get("user");
				map.put("hasFollowed", judgeFollowed(u.getId()));
			}
		}
		
		model.addAttribute("users", list);
		return "/site/followee";
	}
	
	@RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
	public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
		User user = userService.getUserById(userId);
		if (user == null) {
			throw new RuntimeException("该用户不存在!");
		}
		model.addAttribute("user", user);
		
		page.setLimit(5);
		page.setPath("/followers/" + userId);
		page.setRows((int) followService.getFolloweeCount(ENTITY_TYPE_USER, userId));
		
		List<Map<String, Object>> userList = followService.getFollowers(userId, page.getOffset(), page.getLimit());
		if (userList != null) {
			for (Map<String, Object> map : userList) {
				User u = (User) map.get("user");
				map.put("hasFollowed", judgeFollowed(u.getId()));
			}
		}
		model.addAttribute("users", userList);
		
		return "/site/follower";
	}
}