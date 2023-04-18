package com.nc.nccommunity.controller;

import com.nc.nccommunity.entity.User;
import com.nc.nccommunity.service.LikeService;
import com.nc.nccommunity.util.CommunityUtil;
import com.nc.nccommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {
	@Autowired
	private LikeService likeService;
	@Autowired
	HostHolder hostHolder;
	
	@PostMapping("/like")
	@ResponseBody
	public String like(int entityType, int entityId){
		User user = hostHolder.getUser();
		likeService.like(user.getId(), entityType, entityId);
		long count = likeService.countLike(entityType, entityId);
		int status = likeService.ifLiked(user.getId(), entityType, entityId)?1:0;
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("likeCount", count);
		map.put("likeStatus", status);
		
		return CommunityUtil.getJSONString(0, null, map);
	}
	
	
}
