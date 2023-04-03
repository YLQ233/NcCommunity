package com.nc.nccommunity.controller;

import com.nc.nccommunity.entity.DiscussPost;
import com.nc.nccommunity.entity.Page;
import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.service.DiscussPostService;
import com.nc.nccommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
	@Autowired
	private DiscussPostService discussPostService;
	@Autowired
	private UserService userService;
	
	@GetMapping("/index")
	public String getIndexPage(Model model, Page page) {
		page.setRows(discussPostService.getDiscussPostRows(0));
		page.setPath("/index");
		
		List<DiscussPost> list = discussPostService.getDiscussPostList(0, page.getOffset(), page.getLimit());
		List<Map<String, Object>> discussPosts = new ArrayList<>();
		if (list != null) {
			for (DiscussPost post : list) {
				Map<String, Object> map = new HashMap<>();
				map.put("post", post);
				User user = userService.getUserById(post.getUserId());
				map.put("user", user);
				discussPosts.add(map);
			}
		}
		model.addAttribute("discussPosts", discussPosts);
		return "/index";
	}
	
}
