package com.nc.nccommunity.controller;

import com.nc.nccommunity.entity.DiscussPost;
import com.nc.nccommunity.entity.Page;
import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.service.DiscussPostService;
import com.nc.nccommunity.service.LikeService;
import com.nc.nccommunity.service.UserService;
import com.nc.nccommunity.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {
	@Autowired
	private DiscussPostService discussPostService;
	@Autowired
	private UserService userService;
	@Autowired
	private LikeService likeService;
	
	@GetMapping("/index")
	public String getIndexPage(Model model, Page page, @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
		// 方法调用钱,SpringMVC会自动实例化Model和Page,并将Page注入Model.
		// 所以,在thymeleaf中可以直接访问Page对象中的数据.
		page.setRows(discussPostService.getDiscussPostRows(0));
		page.setPath("/index?orderMode=" + orderMode);
		
		List<DiscussPost> list = discussPostService.getDiscussPostList(0, page.getOffset(), page.getLimit(), orderMode);
		List<Map<String, Object>> discussPosts = new ArrayList<>();
		if (list != null) {
			for (DiscussPost post : list) {
				Map<String, Object> map = new HashMap<>();
				map.put("post", post);
				User user = userService.getUserById(post.getUserId());
				map.put("user", user);
				
				long likeCount = likeService.countLikeEntity(ENTITY_TYPE_POST, post.getId());
				map.put("likeCount", likeCount);
				
				discussPosts.add(map);
			}
		}
		model.addAttribute("orderMode", orderMode);
		model.addAttribute("discussPosts", discussPosts);
		return "/index";
	}
	
	@GetMapping("/error")
	private String getErrorPage(){
		return "/error/500";
	}
	
	@GetMapping("/denied")
	public String getDeniedPage() {
		return "/error/404";
	}
	
	
}
